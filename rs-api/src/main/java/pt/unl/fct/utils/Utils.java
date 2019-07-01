package pt.unl.fct.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import pt.unl.fct.errors.BadRequestException;
import pt.unl.fct.errors.NotFoundException;
import pt.unl.fct.model.FetchData;

import java.io.IOException;

public final class Utils {

    private static String RS_ENGINE_ENDPOINT_IP = Utils.getEnvVariable("RS_ENGINE_ENDPOINT_IP", "127.0.0.1");
    private static String RS_ENGINE_ENDPOINT_PORT = Utils.getEnvVariable("RS_ENGINE_ENDPOINT_PORT", "8081");
    private static String WORK_REST_ENDPOINT = "http://" + RS_ENGINE_ENDPOINT_IP + ":" + RS_ENGINE_ENDPOINT_PORT + "/work";

    private static String getEnvVariable(String envVarName, String defaultValue) {
        try {
            String variable = System.getenv(envVarName);
            if (variable == null) throw new Exception();
            else return variable;
        } catch (Exception e) {
            return defaultValue;
        }
    }


    public static void fetchHttpPost(FetchData fetchData) throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(WORK_REST_ENDPOINT);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(fetchData);

        StringEntity stringEntity = new StringEntity(json);

        httpPost.setEntity(stringEntity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        CloseableHttpResponse response = client.execute(httpPost);

        if (response.getStatusLine().getStatusCode() != 202) {
            String entityString = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (response.getStatusLine().getStatusCode() == 404){
                throw new NotFoundException(entityString);
            }
            else if (response.getStatusLine().getStatusCode() == 400)
                throw new BadRequestException(entityString);
        }

        client.close();
    }

}


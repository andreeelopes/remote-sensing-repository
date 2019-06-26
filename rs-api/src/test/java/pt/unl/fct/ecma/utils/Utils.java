//package pt.unl.fct.ecma.utils;
//
//import com.fasterxml.jackson.databind.JavaType;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.User;
//
//import java.io.IOException;
//import java.util.List;
//
//import static java.util.Collections.emptyList;
//
//public class Utils {
//
//    public static <T> List<T> toList(ObjectMapper objectMapper, String jsonArray, JavaType type) throws IOException {
//
//        JsonParser jsonParser = new JsonParser();
//        JsonElement element = jsonParser.parse(jsonArray);
//        JsonObject array = element.getAsJsonObject();
//        String content = array.get("content").toString();
//
//        return objectMapper.readValue(content, type);
//    }
//
//    public static void authenticateUser(String username, String password) {
//
//        Authentication auth = new UsernamePasswordAuthenticationToken(new User(username, password,emptyList()), password);
//        SecurityContext securityContext = SecurityContextHolder.getContext();
//
//        securityContext.setAuthentication(auth);
//    }
//
//}

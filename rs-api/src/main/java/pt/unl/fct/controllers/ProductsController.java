package pt.unl.fct.controllers;


import io.swagger.annotations.ApiParam;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.unl.fct.api.ProductsApi;
import pt.unl.fct.errors.BadRequestException;
import pt.unl.fct.errors.ConflictException;
import pt.unl.fct.errors.InternalServerException;
import pt.unl.fct.errors.NotFoundException;
import pt.unl.fct.model.CustomMetadata;
import pt.unl.fct.model.FetchData;
import pt.unl.fct.model.Product;
import pt.unl.fct.model.SchemaJson;
import pt.unl.fct.services.ProductService;
import pt.unl.fct.services.SchemaService;
import pt.unl.fct.utils.Utils;

import javax.validation.Valid;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
public class ProductsController implements ProductsApi {

    @Autowired
    private ProductService productService;
    @Autowired
    private SchemaService schemaService;


    @Override
    public Object getProduct(@ApiParam(value = "Product ID", required = true) @PathVariable("productId") String productId) {
        return productService.getProduct(productId);
    }

    @Override
    public ResponseEntity<Resource> getProductData(@ApiParam(value = "ID of the product", required = true) @PathVariable("productId") String productId,
                                                   @ApiParam(value = "ID of the data object", required = true) @PathVariable("dataId") String dataId) throws IOException {


        String productDataLocation = productService.getProductDataLocation(productId, dataId); //checks if product data exists
        String[] splitPath = productDataLocation.split("/");
        String filename = splitPath[splitPath.length - 1];

        File file = new File(productDataLocation);

        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    @Override
    public void fetchProductData(@ApiParam(value = "Data object to be fetched") @Valid @RequestBody FetchData fetchData) {
        productService.fetchProductData(fetchData);
    }

    @Override
    public void addProduct(@ApiParam(value = "Zip file with product data") @RequestParam(value = "file") MultipartFile file,
                           @ApiParam(value = "Product metadata") @RequestParam(value = "metadata") JSONObject metadata) {

        try {
            productService.getProduct(metadata.getString("_id"));
            throw new ConflictException("Product with the given id already exists");
        } catch (JSONException e) {
            throw new BadRequestException("Malformed json - " + e.getMessage());
        } catch (NotFoundException ignored) {
        }

        schemaService.validateSchema(metadata);

        try {
            File zip = File.createTempFile(UUID.randomUUID().toString(), "temp");
            file.transferTo(zip);

            try {
                ZipFile zipFile = new ZipFile(zip);
                zipFile.extractAll(Utils.BASE_DIR + "/" + metadata.getString("_id"));
            } catch (ZipException e) {
                e.printStackTrace();
                throw new InternalServerException("Uploaded file is not a zip");
            } finally {
                zip.delete();
            }


        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalServerException("Error while saving product data to disk");
        }


        productService.addProduct(metadata);
    }

    @Override
    public void updateProduct(@ApiParam(value = "Product ID", required = true) @PathVariable("productId") String productId,
                              @ApiParam(value = "Custom metadata to be added", required = true) @Valid @RequestBody CustomMetadata customMetadata) {
        productService.updateProduct(customMetadata);
    }


    @Override
    public void deleteProductData(String productId, String dataId) {
        //TODO
    }

    @Override
    public Page<Product> getProducts(@ApiParam(value = "MongoDB Query") @Valid @RequestBody(required = false) String mongoQuery,
                                     @ApiParam(value = "Page") @RequestParam(defaultValue = "0", required = false) String page,
                                     @ApiParam(value = "Page Size") @RequestParam(defaultValue = "100", required = false) String pageSize) {
        try {
            new BasicQuery(mongoQuery);
        } catch (Exception e) {
            throw new BadRequestException("Malformed query");
        }

        return productService.getProducts(mongoQuery, Integer.parseInt(page), Integer.parseInt(pageSize));
    }

}

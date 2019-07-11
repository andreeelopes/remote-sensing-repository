package pt.unl.fct.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import pt.unl.fct.errors.NotFoundException;
import pt.unl.fct.model.FetchData;
import pt.unl.fct.model.Product;
import pt.unl.fct.utils.Utils;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;

@Service
public class ProductService {

    private static final String PRODUCTS_MD_COL = "productsMD";
    private static final String SCHEMA_COL = "schema";

    @Autowired
    private MongoTemplate mongoTemplate;

    public Object getProduct(String productId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(productId));

        Object product = mongoTemplate.findOne(query, Object.class, PRODUCTS_MD_COL);

        if (product != null) {
            return product;
        } else throw new NotFoundException(String.format("Product with id %s does not exist", productId));
    }

    public Page<Product> getProducts(String search, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Query query = new BasicQuery(search).with(pageable);

        List<Product> products = mongoTemplate.find(query, Product.class, PRODUCTS_MD_COL);

        return PageableExecutionUtils.getPage(
                products,
                pageable,
                () -> mongoTemplate.count(query, Product.class));
    }

    public void fetchProductData(FetchData fetchData) throws IOException {
        Utils.fetchHttpPost(fetchData);
    }


    public List<Object> getSchema() {
        return mongoTemplate.findAll(Object.class, SCHEMA_COL);
    }
}

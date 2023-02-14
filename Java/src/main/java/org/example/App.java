package org.example;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class App implements RequestHandler<Object, Object> {

    private DynamoDB dynamoDb;
    private static final String DYNAMODB_TABLE_NAME = "http-crud-items";
    static String projectionExpression = "id,age,power";
    private final Regions REGION = Regions.US_EAST_1;

    public App() {
        super();
        initDynamoDbClient();
    }

    @Override
    public Object handleRequest(Object o, Context context) {
        List<PersonRequest> responseList = getPapers(dynamoDb);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setMessage("Lista retornada!");

        return responseList;
    }

/*    @Override
    public PersonResponse handleRequest(PersonRequest personRequest, Context context) {

        this.initDynamoDbClient();

        persistData(personRequest);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setMessage("Salvou com sucesso!");

        return personResponse;
    }*/

    private PutItemOutcome persistData(PersonRequest personRequest)
            throws ConditionalCheckFailedException {
        return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                .putItem(
                        new PutItemSpec().withItem(new Item()
                                .withString("id", personRequest.getId())
                                .withString("name", personRequest.getName())
                                .withString("age", personRequest.getAge())
                                .withString("power", personRequest.getPower())
                        )
                );
    }

    private void initDynamoDbClient() {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(REGION));
        this.dynamoDb = new DynamoDB(client);
    }

    public List<PersonRequest> getPapers(DynamoDB dynamoDb) {
        Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
        ScanSpec scanSpec = new ScanSpec().withProjectionExpression(projectionExpression);
        List<PersonRequest> responseList = new ArrayList<>();
        try {
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);
            for (Item item : items) {
                PersonRequest res = new PersonRequest();

                res.setId(item.getString("id"));
                res.setName(item.getString("name"));
                res.setAge(item.getString("age"));
                res.setPower(item.getString("power"));
                responseList.add(res);
            }
        } catch (Exception e) {
            System.err.println("Unable to scan the table:");
            System.err.println(e.getMessage());
        }
        return responseList;
    }

}

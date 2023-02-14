const AWS = require("aws-sdk");

const dynamo = new AWS.DynamoDB.DocumentClient();

exports.handler = async (event, context) => {
    let body;
    let statusCode = 200;
    const headers = {
        "Content-Type:": "application/json",
        "Access-Control-Allow-Headers" : "Content-Type",
        "Access-Control-Allow-Origin": "http://localhost:3000",
        "Access-Control-Allow-Methods": "*"
    };
    
    try{
        switch (event.routeKey) {
            case 'DELETE /items/{id}':
                await dynamo.delete({
                    TableName: "http-crud-items",
                    Key: {
                        id: event.pathParameters.id
                    }
                })
                .promise();
                body = `Delete item ${event.pathParameters.id}`;
                break;
                
            case "GET /items":
                body = await dynamo.scan({ TableName: "http-crud-items" }).promise();
                break;
                
            case 'PUT /items':
                let requestJSON = JSON.parse(event.body);
                await dynamo.put({
                    TableName: "http-crud-items",
                    Item: {
                        id: requestJSON.id,
                        name: requestJSON.name,
                        age: requestJSON.age,
                        power: requestJSON.power,
                    }
                })
                .promise();
                body = `Put item ${requestJSON.id}`;
                break;
                
            default:
                throw new Error(`Unsupported route: "${event.routeKey}"`);
                
        }
    } catch (err){
        statusCode = 400;
        body = err.message;
    } finally {
        body = JSON.stringify(body);
    }
    
    return {
        statusCode,
        body,
        headers
    };
};

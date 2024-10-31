package com.example.mongoApp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MongoEx1 extends AbstractVerticle {

  public void start() {
    JsonObject config = new JsonObject()
      .put("connection_string",  "mongodb://localhost:27017")
      .put("db_name", "vertxUsers");

    MongoClient mongoClient = MongoClient.createShared(vertx, config);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.get("/users").handler(routingCtx -> getUsers(routingCtx , mongoClient));
    router.post("/addUser").handler(routingCtx -> insertUser(routingCtx , mongoClient));
    router.delete("/deleteUser").handler(routingCtx -> deleteUser(routingCtx, mongoClient));
    router.put("/updateUser").handler(routingCtx -> updateUser(routingCtx, mongoClient));

    vertx.createHttpServer().requestHandler(router).listen(8000 , http -> {
      if(http.succeeded()){
        System.out.println("server is running");
      }else {
        System.out.println("error : " + http.cause());
      }
    });
  }

  public void getUsers(RoutingContext context, MongoClient mongoClient) {
    JsonObject users = new JsonObject();

    mongoClient.find("users" ,users , res -> {
      if(res.succeeded()){
        context.response().end(res.result().toString());
      }else {
        context.response().setStatusCode(500).end("error" + res.cause());
      }
    });
  }

  public void insertUser(RoutingContext ctx , MongoClient mongoClient) {
    try {
      JsonObject userData = ctx.getBodyAsJson();
      String prenom = userData.getString("prenom");
      String nom = userData.getString("nom");
      String age = userData.getString("age");

      JsonObject newUser = new JsonObject()
        .put("prenom", prenom)
        .put("nom" , nom)
        .put("age" , age);

      mongoClient.insert("users" , newUser , res -> {
        if(res.succeeded()){
          ctx.response().end("users has been added " + nom );
        }else {
          ctx.response().end("error" + res.cause());
        }
      });
    }catch(Exception e) {
      System.out.println("error " + e);
    }
  }

  public void deleteUser(RoutingContext ctx , MongoClient mongoClient) {
    try{
      JsonObject toDelete = ctx.getBodyAsJson();
      String nom = toDelete.getString("nom");

      JsonObject UserToDelete = new JsonObject().put("nom" , nom);

      mongoClient.findOneAndDelete("users" , UserToDelete , res -> {
        if(res.succeeded()){
          ctx.response().end("users has been deleted " + nom );
        }else {
          ctx.response().setStatusCode(500).end("error " + res.cause());
        }
      });
    }catch (Exception e) {
      System.out.println("error" + e);
    }
  }

  public void updateUser(RoutingContext ctx , MongoClient mongoClient) {
    JsonObject data = ctx.getBodyAsJson();
    String toUpdateUserName = data.getString("toUpdateUserName");
    String nom = data.getString("nom");
    String prenom = data.getString("prenom");
    String age = data.getString("age");

    JsonObject toUpdate = new JsonObject().put("nom", toUpdateUserName);

    JsonObject update = new JsonObject().put("$set", new JsonObject()
      .put("nom" , nom)
      .put("prenom" , prenom)
      .put("age" , age));

    mongoClient.findOneAndUpdate("users",toUpdate ,update , res -> {
      if(res.succeeded()){
        ctx.response().end("users has been updated " + nom );
      }else {
        ctx.response().setStatusCode(500).end("error " + res.cause());
      }
    });
  }

  public static void main(String[] args) {
    Vertx vertx1 = Vertx.vertx();
    vertx1.deployVerticle(new MongoEx1());
  }
}

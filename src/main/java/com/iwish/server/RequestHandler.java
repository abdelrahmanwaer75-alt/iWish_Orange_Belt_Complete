package com.iwish.server;

import com.iwish.common.Request;
import com.iwish.common.Response;

public class RequestHandler {
    private final DatabaseManager db;

    public RequestHandler(DatabaseManager db){this.db=db;}

    public Response handle(Request r){
        try{
            return switch(r.action){
                case "REGISTER" -> db.register(r.data);
                case "LOGIN" -> db.login(r.data);
                case "USERS" -> db.users(r.data);
                case "FRIENDS" -> db.friends(r.data);
                case "ADD_FRIEND" -> db.addFriend(r.data);
                case "FRIEND_DECISION" -> db.friendDecision(r.data);
                case "REMOVE_FRIEND" -> db.removeFriend(r.data);
                case "ITEMS" -> db.items();
                case "MY_WISHLIST" -> db.myWishlist(r.data);
                case "FRIEND_WISHLIST" -> db.friendWishlist(r.data);
                case "ADD_WISH" -> db.addWish(r.data);
                case "UPDATE_WISH" -> db.updateWish(r.data);
                case "DELETE_WISH" -> db.deleteWish(r.data);
                case "CONTRIBUTE" -> db.contribute(r.data);
                case "NOTIFICATIONS" -> db.notifications(r.data);
                case "MARK_READ" -> db.markRead(r.data);
                default -> Response.fail("Unknown request: "+r.action);
            };
        }catch(Exception e){
            return Response.fail("Server error: "+e.getMessage());
        }
    }
}
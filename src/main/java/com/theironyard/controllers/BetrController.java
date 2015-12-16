package com.theironyard.controllers;

import com.braintreegateway.*;
import com.braintreegateway.test.Nonce;
import com.theironyard.entities.Community;
import com.theironyard.entities.Post;
import com.theironyard.entities.Press;
import com.theironyard.entities.User;
import com.theironyard.services.CommunityRepository;
import com.theironyard.services.PostRepository;
import com.theironyard.services.PressRepository;
import com.theironyard.services.UserRepository;
import com.theironyard.utils.PasswordHash;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jessicahuffstutler on 12/7/15.
 */
@RestController
public class BetrController {

    @Autowired
    UserRepository users;

    @Autowired
    PostRepository posts;

    @Autowired
    CommunityRepository communities;

    @Autowired
    PressRepository pressPosts;

    private static BraintreeGateway gateway = new BraintreeGateway(
            Environment.SANDBOX,
            "r4pm2vfwphd4pnfh",
            "fkd575nb39thx694",
            "f76bfbc6d5ea0bbfc9caed00077353b3"
    );
    @RequestMapping(path = "/client_token", method = RequestMethod.GET)//http://localhost:8080/client_token
    public Object token() {
        return gateway.clientToken().generate();
    }

    @RequestMapping(path = "/client_token", method = RequestMethod.POST)
    public Object addToken() {
        return gateway.clientToken().generate();
    }
    @RequestMapping(path = "/customer", method = RequestMethod.POST)
    public Object addCustomer(@RequestBody Customer customer){
        CustomerRequest request = new CustomerRequest()
                .firstName("")
                .lastName("")
                .company("")
                .email("")
                .phone("")
                .website("");
        Result<Customer> result = gateway.customer().create(request);

        //return result.isSuccess();
// true
        return result.getTarget().getId();
    }

    @RequestMapping(path = "/checkout", method = RequestMethod.GET)
    public Object getCheckout(@RequestBody Nonce nonce) {

        TransactionRequest request = new TransactionRequest() //http://localhost:8080/checkout?nonce=fake-valid-nonce
                .customerId("")
                .amount(new BigDecimal(""))
                .paymentMethodNonce("");

        Result<Transaction> result = gateway.transaction().sale(request);
        return (result);
    }
    @RequestMapping(path = "/checkout", method = RequestMethod.POST)
    public Object addCheckout(@RequestBody Nonce nonce) {

        TransactionRequest request = new TransactionRequest()
                .amount(new BigDecimal(""))
                .paymentMethodNonce("");

        Result<Transaction> result = gateway.transaction().sale(request);
        return (nonce);
    }
    @RequestMapping(path = "/checkout", method = RequestMethod.PUT)
    public Object putCheckout(@RequestBody Nonce nonce) {

        TransactionRequest request = new TransactionRequest() //http://localhost:8080/checkout?nonce=fake-valid-nonce
                .customerId("")
                .amount(new BigDecimal(""))
                .paymentMethodNonce("");

        Result<Transaction> result = gateway.transaction().sale(request);
        return (result);
    }

    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public User getUser(HttpSession session) {
        String email = (String) session.getAttribute("email");
        User user = users.findOneByEmail(email);
        return user;
    }
    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public void addUser(@RequestBody User user) {

        users.save(user);
    }
    @RequestMapping(path = "/user", method = RequestMethod.PUT)
    public void editUser(@RequestBody User user) {
        users.save(user);
    }
    @RequestMapping(path = "/user{id}", method = RequestMethod.DELETE)
    public void deleteUser (HttpSession session, @PathVariable("id") int id) {
        users.delete(id);
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public void login(HttpSession session, @RequestBody User user) throws Exception {

        User currentUser = users.findOneByEmail(user.email);
        if (currentUser == null) {
            throw new Exception("User not found.");
        } else if (!PasswordHash.validatePassword(user.password, currentUser.password)) {
            throw new Exception("Wrong password");
        }

        session.setAttribute("email", user.email);
    }

    @RequestMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
        System.out.println("Successfully logged out");
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public void register(HttpSession session, @RequestBody User user) throws Exception {

        User currentUser = users.findOneByEmail(user.email);
        if (currentUser == null) {
//            user = new User();
            if (user.email.toLowerCase().equals("wilsonkate.kw@gmail.com") || user.email.equals("jessica.huffstutler@gmail.com") || user.email.equals("info@betrapp.co")) {
                user.isAdmin = true;
            } else {
                user.isAdmin = false;
            }
            user.password = PasswordHash.createHash(user.password);
            users.save(user);

//            user.firstName = firstName;
//            user.lastName = lastName;
//            user.email = email;
//            user.password = PasswordHash.createHash(password);
//            user.isAdmin = isAdmin;
//            users.save(user);
        } else {
            throw new Exception("An account with that email already exists.");
        }

        users.save(user);
        session.setAttribute("email", user.email);
    }


//    @RequestMapping(path = "/press", method = RequestMethod.DELETE)
//    public List<Press> deletePress()

    @RequestMapping(path = "/press", method = RequestMethod.GET)
    public List<Press> getPress(HttpSession session) throws Exception {
        String email = (String) session.getAttribute("email");

        return (List<Press>) pressPosts.findAll();
    }

    @RequestMapping(path = "/press", method = RequestMethod.POST)
    public void addPress(HttpSession session, @RequestBody Press press) throws Exception {
        String email = (String) session.getAttribute("email");
        pressPosts.save(press);
    }

    @RequestMapping(path = "/press", method = RequestMethod.PUT)
    public void editPress(HttpSession session, @RequestBody Press press) throws Exception {
        String email = (String) session.getAttribute("email");
        pressPosts.save(press);
    }

    @RequestMapping(path = "/posts", method = RequestMethod.GET)
    public List<Post> getPosts(HttpSession session) throws Exception {
        String email = (String) session.getAttribute("email");

        return (List<Post>) posts.findAll();
    }

    @RequestMapping(path = "/posts", method = RequestMethod.POST)
    public void addPost(HttpSession session, @RequestBody Post post) throws Exception {
//        String email = (String) session.getAttribute("email");

//        if (username == null) {
//            throw new Exception("You are not logged in.");
//        }
//         if (!postImage.getContentType().startsWith("image")){
//             throw new Exception("Only images are allowed!");
//         }
//         File photoFile = File.createTempFile("postImage", postImage.getOriginalFilename(), new File("public"));
//         FileOutputStream fos = new FileOutputStream(photoFile);
//         fos.write(postImage.getBytes()); //to save to a file in the public folder
//
//        Post post = new Post();
//        post.communityName = communityName; //this should be a dropdown menu for the admin to select a community to avoid spelling errors
//        post.postName = postName;
//        post.postBody = postBody;
//        post.postTime = postTime;
//        post.filename = photoFile.getName();
        posts.save(post);
    }

    @RequestMapping(path = "/posts", method = RequestMethod.PUT)
    public void editPost(HttpSession session, @RequestBody Post post) throws Exception {
        String email = (String) session.getAttribute("email");

//        if (username == null) {
//            throw new Exception("You are not logged in.");
//        }

//        Post post = posts.findOne(id);
//        if (post.communityName!=null){
//                post.communityName = communityName; //this should be a dropdown menu for the admin to select a community to avoid spelling errors
//        }
//        if (post.postName!=null){
//               post.postName = postName;
//        }
//        if (post.postBody!=null){
//                post.postBody = postBody;
//        }
//        if (!postImage.isEmpty()) {
//            if (!postImage.getContentType().startsWith("image")) {
//                throw new Exception("Only images are allowed!");
//            }
//            File photoFile = File.createTempFile("postImage", postImage.getOriginalFilename(), new File("public"));
//            FileOutputStream fos = new FileOutputStream(photoFile);
//            fos.write(postImage.getBytes()); //to save to a file in the public folder
//        }
        posts.save(post);
    }

    @RequestMapping(path = "/posts/{id}", method = RequestMethod.DELETE)
    public void deletePost(HttpSession session, @PathVariable("id") int id) throws Exception {
        String email = (String) session.getAttribute("email");
//        if (email == null) {
//            throw new Exception("You are not logged in.");
//        }

        Post post = posts.findOne(id);
        posts.delete(post);
    }

    @RequestMapping(path = "/community", method = RequestMethod.POST)
       public void addCommunity(HttpSession session, @RequestBody Community community) throws Exception {
//        String email = (String) session.getAttribute("email");
//        if (email == null) {
//            throw new Exception("You are not logged in.");
//        }

//        if (!image.getContentType().startsWith("image")){
//            throw new Exception("Only images are allowed!");
//        }
//        File photoFile = File.createTempFile("communityImage", communityImage.getOriginalFilename(), new File("public"));
//        FileOutputStream fos = new FileOutputStream(photoFile);
//        fos.write(communityImage.getBytes()); //to save to a file in the public folder
//
//        Community community = new Community();
//        community.name = name;
//        community.population = population;
//        community.goal = goal;
//        community.description = description;
//        community.filename = photoFile.getName();

        communities.save(community);
    }
    
    @RequestMapping(path = "/community/{id}", method = RequestMethod.DELETE)
    public void deleteCommunity(HttpSession session, @PathVariable("id") int id) throws Exception {
        String email = (String) session.getAttribute("email");
//        if (email == null) {
//            throw new Exception("You are not logged in.");
//        }

        Community community = communities.findOne(id);
        communities.delete(community);
    }

    @RequestMapping(path = "/community", method = RequestMethod.PUT)
    public void editCommunity(HttpSession session, @RequestBody Community community) throws Exception {
        String email = (String) session.getAttribute("email");
//        if (email == null) {
//            throw new Exception("You are not logged in.");
//        }
//
//        Community community = communities.findOne(id);  //When updating the previous input will remain.
//        if (community.name!=null){
//            community.name = name;
//        }
//        if (community.population!=0){
//            community.population = population;
//        }
//        if (community.goal!=0) {
//            community.goal = goal;
//        }
//        if (community.description!=null){
//            community.description = description;
//        }
//        if (!communityImage.isEmpty()){
//            if (!communityImage.getContentType().startsWith("image")){
//                throw new Exception("Only images are allowed!");
//            }
//            File photoFile = File.createTempFile("communityImage", communityImage.getOriginalFilename(), new File("public"));
//            FileOutputStream fos = new FileOutputStream(photoFile);
//            fos.write(communityImage.getBytes());
//        }
        communities.save(community);
    }

    @RequestMapping(path = "/communities", method = RequestMethod.GET)
    public List<Community> getCommunities(HttpSession session) throws Exception {
        String email = (String) session.getAttribute("email");

        return (List<Community>) communities.findAll();
    }

    @RequestMapping(value = "/userInformation", method = RequestMethod.GET)
    public void generateCsvFile(HttpServletResponse response) throws Exception {
        ArrayList<User> usersList = (ArrayList<User>) users.findAll();

        if(users == null) {
            throw new Exception("There are no users.");
        } else {
            try{
                StringBuilder writer = new StringBuilder();

                writer.append("FirstName");
                writer.append(',');
                writer.append("LastName");
                writer.append(',');
                writer.append("Email");
                writer.append('\n');

                for (User user : usersList) {
                    writer.append(user.firstName);
                    writer.append(',');
                    writer.append(user.lastName);
                    writer.append(',');
                    writer.append(user.email);
                    writer.append('\n');
                }

                response.getOutputStream().write(writer.toString().getBytes());
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", String.format("Attachment: filename = %s_usersList.csv", LocalDateTime.now()));

            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}

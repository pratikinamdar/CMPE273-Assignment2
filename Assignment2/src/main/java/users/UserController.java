package users;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.Valid;

import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;


@RestController
public class UserController extends WebMvcConfigurerAdapter {
	final AtomicLong userId = new AtomicLong();
	final AtomicLong idCardLong = new AtomicLong();
	private final AtomicLong webLoginLong = new AtomicLong();
	private final AtomicLong bankAccLong = new AtomicLong();
	SimpleDateFormat sdf = null;
	Date date = null;
	String emptyString = "";

	
	public static DBCollection getCollection(String collectionName) throws UnknownHostException, MongoException {
		String uri = "mongodb://ds047050.mongolab.com:47050/";
		MongoCredential mongoCredential = MongoCredential.createMongoCRCredential("pratikinamdar", "cmpe273", "Gr0undnut@123".toCharArray());
		MongoClientURI mongoClientURI = new MongoClientURI(uri);

		MongoClient mongoClient = new MongoClient(mongoClientURI);

		DB db = mongoClient.getDB("cmpe273");
		db.authenticate("pratikinamdar", "Gr0undnut123".toCharArray());

		DBCollection dbobj = db.getCollection(collectionName);
		return dbobj;
	}

	
	@RequestMapping(value = "API/V1/users", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<UserInfo> createUser(@RequestBody @Valid UserInfo user, BindingResult bindingResult) throws UnknownHostException, MongoException {
		ResponseEntity<UserInfo> respEntity = null;
		DBCollection collection = UserController.getCollection("users");

		DBObject objQuery;
		DBCursor cursor;

		if (bindingResult.hasErrors()) {
			respEntity = new ResponseEntity<UserInfo>(user, HttpStatus.BAD_REQUEST);
		} else {
			Long newUserId = userId.incrementAndGet();
			String newUserIdStr = "u-" + newUserId;

			date = new Date();
			sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
			user.setCreated_at(sdf.format(date));
			user.setUpdated_at(sdf.format(date));
			
			UserInfo newUser = new UserInfo(newUserIdStr, user.getEmail(),
					user.getPassword(), 
					user.getCreated_at(), user.getUpdated_at());

			// Checks if the USer already exists,
			// if exists creates new User ID
			cursor = collection.find();
			String existUser = null;
			while (cursor.hasNext()) {
				objQuery = cursor.next();
				existUser = objQuery.get("user_id").toString();
				if (existUser != null && newUserIdStr.equalsIgnoreCase(existUser)) {
					newUserId = userId.incrementAndGet();
					newUserIdStr = "u-" + newUserId;
				}
			}

			// Insert the new User in the MongoDB collection
			BasicDBObject objToInsert = new BasicDBObject("user_id", newUserIdStr)
					.append("email", newUser.getEmail())
					.append("password", newUser.getPassword())
					
					.append("created_at", newUser.getCreated_at())
					.append("updated_at", newUser.getUpdated_at());

			collection.insert(objToInsert);

			respEntity = new ResponseEntity<UserInfo>(newUser, HttpStatus.CREATED);

		}
		return respEntity;
	}

	@RequestMapping(value = "/API/V1/Users/{user_id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<UserInfo> viewUser(@PathVariable String user_id) throws UnknownHostException, MongoException {
		UserInfo user = new UserInfo();
		ResponseEntity<UserInfo> respEntity = null;
		DBCollection collection = UserController.getCollection("users");
		DBObject obj = null;
		DBObject objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).get();
		DBCursor cursor = collection.find(objQuery);

		if (cursor.hasNext()) {
			obj = cursor.next();
			// obj.removeField("_id");
			user.setUser_id(obj.get("user_id").toString());
			user.setEmail(obj.get("email").toString());
			user.setPassword(obj.get("password").toString());
			
			user.setCreated_at(obj.get("created_at").toString());
			user.setUpdated_at(obj.get("updated_at").toString());
			respEntity = new ResponseEntity<UserInfo>(user, HttpStatus.OK);
		} else {
			respEntity = new ResponseEntity<UserInfo>(user, HttpStatus.NO_CONTENT);
		}
		return respEntity;
	}

	@RequestMapping(value = "/API/V1/Users/{user_id}", method = RequestMethod.PUT)
	public ResponseEntity<UserInfo> updateUser(@RequestBody @Valid UserInfo user, @PathVariable String user_id, BindingResult bindingResult) throws UnknownHostException, MongoException {
		date = new Date();
		sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		UserInfo updatedUser = new UserInfo();
		ResponseEntity<UserInfo> respEntity = null;
		DBObject obj = null;
		DBObject newObj = null;
		DBObject objQuery = null;
		DBCursor cursor = null;
		DBCollection collection = UserController.getCollection("users");

		if (bindingResult.hasErrors()) {
			respEntity = new ResponseEntity<UserInfo>(user, HttpStatus.BAD_REQUEST);
		} else {
			objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).get();
			cursor = collection.find(objQuery);
			
			if (cursor.hasNext()) {
				obj = cursor.next();
				
				newObj = new BasicDBObject("user_id", user_id)
					.append("email", user.getEmail())
					.append("password", user.getPassword())
					
					.append("created_at", obj.get("created_at").toString())
					.append("updated_at", sdf.format(date));

				collection.findAndModify(obj, newObj);
				
				updatedUser.setUser_id(newObj.get("user_id").toString());
				updatedUser.setEmail(newObj.get("email").toString());
				updatedUser.setPassword(newObj.get("password").toString());
				
				updatedUser.setCreated_at(newObj.get("created_at").toString());
				updatedUser.setUpdated_at(newObj.get("updated_at").toString());

				respEntity = new ResponseEntity<UserInfo>(updatedUser, HttpStatus.CREATED);
			} else {
				respEntity = new ResponseEntity<UserInfo>(updatedUser, HttpStatus.NO_CONTENT);
			}
		}
		return respEntity;
	}

	
    @RequestMapping(value="/API/V1/Users/{user_id}/idcards", method=RequestMethod.POST, consumes="application/json")
    public ResponseEntity<CardInfo> createIdCard(@RequestBody @Valid CardInfo idCard, @PathVariable String user_id, BindingResult bindingResult) throws UnknownHostException, MongoException {
    	ResponseEntity<CardInfo> respEntity = null;
    	DBCollection usersCollection = UserController.getCollection("users");
		DBCollection idCardscollection = UserController.getCollection("idcards");
		DBObject objQuery;
		DBObject objIdCardToInsert;
		DBCursor cursor;
		DBCursor cursorIdCards;
		Long newCardId;
		String newCardIdStr = emptyString;
		String idCardExist = emptyString;
		
		if (bindingResult.hasErrors()) {
    		respEntity = new ResponseEntity<CardInfo>(idCard, HttpStatus.BAD_REQUEST);
        } else {
        	objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).get();
			cursor = usersCollection.find(objQuery);
			if(cursor.hasNext()) {
				newCardId = idCardLong.incrementAndGet();
		    	newCardIdStr = "c-" + newCardId;
		    	
		    	cursorIdCards = idCardscollection.find();
				while (cursorIdCards.hasNext()) {
					objQuery = cursorIdCards.next();
					idCardExist = objQuery.get("card_id").toString();
					if (idCardExist != null && newCardIdStr.equalsIgnoreCase(idCardExist)) {
						newCardId = idCardLong.incrementAndGet();
				    	newCardIdStr = "c-" + newCardId;
					}
				}

				idCard.setCard_id(newCardIdStr);
				// Insert the new User in the MongoDB collection
				objIdCardToInsert = new BasicDBObject();
					objIdCardToInsert.put("user_id", user_id);
					objIdCardToInsert.put("card_id", idCard.getCard_id());
					objIdCardToInsert.put("card_name", idCard.getCard_name());
					objIdCardToInsert.put("card_number", idCard.getCard_id());
					objIdCardToInsert.put("expiration_date", idCard.getExpiration_date());
				idCardscollection.insert(objIdCardToInsert);
		    	
				respEntity = new ResponseEntity<CardInfo>(idCard, HttpStatus.CREATED);
			} else {
				respEntity = new ResponseEntity<CardInfo>(idCard, HttpStatus.NO_CONTENT);
			}
		}
    	return respEntity;
    }

  
    @RequestMapping(value="/API/V1/Users/{user_id}/idcards", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<List<CardInfo>> listAllIdCard(@PathVariable String user_id) throws UnknownHostException, MongoException {
    	List<CardInfo> idCardsList = new ArrayList<CardInfo>();
    	CardInfo idCard = null;
    	ResponseEntity<List<CardInfo>> respEntity = null;
    	DBCollection idCardscollection = UserController.getCollection("idcards");
    	DBObject objQuery;
		DBCursor cursor;
    	
    	objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).get();
		cursor = idCardscollection.find(objQuery);
		if(cursor.hasNext()) {
			while(cursor.hasNext()) {
				objQuery = cursor.next();
				idCard = new CardInfo();
				
				idCard.setCard_id(objQuery.get("card_id").toString());
				idCard.setCard_name(objQuery.get("card_name").toString());
				idCard.setCard_id(objQuery.get("card_number").toString());
				idCard.setExpiration_date(objQuery.get("expiration_date").toString());
				idCardsList.add(idCard);
			}
			respEntity = new ResponseEntity<List<CardInfo>>(idCardsList, HttpStatus.OK);
		} else {
			respEntity = new ResponseEntity<List<CardInfo>>(idCardsList, HttpStatus.NO_CONTENT);
		}
    	return respEntity;
    }
    
    
    @RequestMapping(value="/API/V1/Users/{user_id}/idcards/{card_id}", method=RequestMethod.DELETE)
    public ResponseEntity<CardInfo> deleteIdCard(@PathVariable String user_id, @PathVariable String card_id) throws UnknownHostException, MongoException {
    	DBCollection idCardscollection = UserController.getCollection("idcards");
    	DBObject objQuery;
		DBCursor cursor;
		CardInfo idCard = null;
    	
    	objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).add("card_id", card_id).get();
		cursor = idCardscollection.find(objQuery);
		if (cursor.hasNext()) {
			objQuery = cursor.next();
			idCardscollection.remove(objQuery);
		}

		return new ResponseEntity<CardInfo>(idCard, HttpStatus.NO_CONTENT); 
    }
    
    
    @RequestMapping(value="/API/V1/Users/{user_id}/LoginInfo", method=RequestMethod.POST, consumes="application/json")
    public ResponseEntity<LoginInfo> createWebLogin(@RequestBody @Valid LoginInfo webLogin, @PathVariable String user_id, BindingResult bindingResult) throws UnknownHostException, MongoException {
    	ResponseEntity<LoginInfo> respEntity = null;
    	DBCollection usersCollection = UserController.getCollection("users");
		DBCollection webLogincollection = UserController.getCollection("LoginInfo");
		DBObject objQuery;
		DBObject objWebLoginToInsert;
		DBCursor cursor;
		DBCursor cursorWebLogin;
		Long newWebLogin;
		String newLoginInfotr = emptyString;
		String webLoginExist = emptyString;
		
		if (bindingResult.hasErrors()) {
    		respEntity = new ResponseEntity<LoginInfo>(webLogin, HttpStatus.BAD_REQUEST);
        } else {
        	objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).get();
			cursor = usersCollection.find(objQuery);
			if(cursor.hasNext()) {
				newWebLogin = webLoginLong.incrementAndGet();
				newLoginInfotr = "l-" + newWebLogin;
		    	
				cursorWebLogin = webLogincollection.find();
				while (cursorWebLogin.hasNext()) {
					objQuery = cursorWebLogin.next();
					webLoginExist = objQuery.get("login_id").toString();
					if (webLoginExist != null && newLoginInfotr.equalsIgnoreCase(webLoginExist)) {
						newWebLogin = webLoginLong.incrementAndGet();
						newLoginInfotr = "l-" + newWebLogin;
					}
				}

				webLogin.setLogin_id(newLoginInfotr);
				// Insert the new WebLogin in the MongoDB collection
				objWebLoginToInsert = new BasicDBObject();
				objWebLoginToInsert.put("user_id", user_id);
				objWebLoginToInsert.put("login_id", webLogin.getLogin_id());
				objWebLoginToInsert.put("url", webLogin.getUrl());
				objWebLoginToInsert.put("login", webLogin.getLogin());
				objWebLoginToInsert.put("password", webLogin.getPassword());
				webLogincollection.insert(objWebLoginToInsert);
		    	
				respEntity = new ResponseEntity<LoginInfo>(webLogin, HttpStatus.CREATED);
			} else {
				respEntity = new ResponseEntity<LoginInfo>(webLogin, HttpStatus.NO_CONTENT);
			}
		}
    	return respEntity;
    }
    
    
    @RequestMapping(value="/API/V1/Users/{user_id}/LoginInfo", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<List<LoginInfo>> listAllLoginInfo(@PathVariable String user_id) throws UnknownHostException, MongoException {
    	
    	List<LoginInfo> webLoginList = new ArrayList<LoginInfo>();
    	ResponseEntity<List<LoginInfo>> respEntity = null;
    	LoginInfo LoginInfo = null;
    	DBCollection webLogincollection = UserController.getCollection("LoginInfo");
    	DBObject objQuery;
		DBCursor cursor;
    	
    	objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).get();
		cursor = webLogincollection.find(objQuery);
		if(cursor.hasNext()) {
			while(cursor.hasNext()) {
				objQuery = cursor.next();
				LoginInfo = new LoginInfo();
				LoginInfo.setLogin_id(objQuery.get("login_id").toString());
				LoginInfo.setUrl(objQuery.get("url").toString());
				LoginInfo.setLogin(objQuery.get("login").toString());
				LoginInfo.setPassword(objQuery.get("password").toString());
				
				LoginInfo.add(LoginInfo);
			}
			respEntity = new ResponseEntity<List<LoginInfo>>(webLoginList, HttpStatus.OK);
		} else {
			respEntity = new ResponseEntity<List<LoginInfo>>(webLoginList, HttpStatus.NO_CONTENT);
		}
    	return respEntity;
    }
    
    
    @RequestMapping(value="/API/V1/Users/{user_id}/LoginInfo/{login_id}", method=RequestMethod.DELETE)
    public ResponseEntity<LoginInfo> deleteWebLogin(@PathVariable String user_id, @PathVariable String login_id) throws UnknownHostException, MongoException {
    	DBCollection webLogincollection = UserController.getCollection("LoginInfo");
    	DBObject objQuery;
		DBCursor cursor;
		LoginInfo LoginInfo = null;
    	
    	objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).add("login_id", login_id).get();
		cursor = webLogincollection.find(objQuery);
		if (cursor.hasNext()) {
			objQuery = cursor.next();
			webLogincollection.remove(objQuery);
		}

		return new ResponseEntity<LoginInfo>(LoginInfo, HttpStatus.NO_CONTENT); 
    }

    
    @RequestMapping(value="/API/V1/Users/{user_id}/BankInfo", method=RequestMethod.POST, consumes="application/json")
    public ResponseEntity<BankInfo> createBankAccount(@RequestBody @Valid BankInfo bankAcc, @PathVariable String user_id, BindingResult bindingResult) throws UnknownHostException, MongoException {
    	ResponseEntity<BankInfo> respEntity = null;
    	DBCollection usersCollection = UserController.getCollection("users");
		DBCollection BankInfoCollection = UserController.getCollection("BankInfo");
		DBObject objQuery;
		DBObject objBankAccountToInsert;
		DBCursor cursor;
		DBCursor cursorBankAccount;
		Long newBankAccount;
		String newBankInfotr = emptyString;
		String bankAccountExist = emptyString;
		
		if (bindingResult.hasErrors()) {
    		respEntity = new ResponseEntity<BankInfo>(bankAcc, HttpStatus.BAD_REQUEST);
        } else {
        	
        	RestTemplate restTemplate = new RestTemplate();
    		ResponseEntity<String> entity = restTemplate.getForEntity("http://www.routingnumbers.info/api/data.json?rn=" + bankAcc.getRouting_number(), String.class);
    		JsonParser jsonParser = new JacksonJsonParser();
     		Map<String,Object> resbody = jsonParser.parseMap(entity.getBody());
    		if((resbody.get("code").toString().equals("200"))) {
    			bankAcc.setAccount_name(resbody.get("customer_name").toString());
    		}
        	
        	objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).get();
			cursor = usersCollection.find(objQuery);
			if(cursor.hasNext()) {
				newBankAccount = bankAccLong.incrementAndGet();
		    	newBankInfotr = "b-" + newBankAccount;
		    	
		    	cursorBankAccount = BankInfoCollection.find();
				while (cursorBankAccount.hasNext()) {
					objQuery = cursorBankAccount.next();
					bankAccountExist = objQuery.get("ba_id").toString();
					if (bankAccountExist != null && newBankInfotr.equalsIgnoreCase(bankAccountExist)) {
						newBankAccount = bankAccLong.incrementAndGet();
				    	newBankInfotr = "b-" + newBankAccount;
					}
				}

				bankAcc.setBa_id(newBankInfotr);
				// Insert the new WebLogin in the MongoDB collection
				objBankAccountToInsert = new BasicDBObject();
				objBankAccountToInsert.put("user_id", user_id);
				objBankAccountToInsert.put("ba_id", bankAcc.getBa_id());
				objBankAccountToInsert.put("account_name", bankAcc.getAccount_name());
				objBankAccountToInsert.put("routing_number", bankAcc.getRouting_number());
				objBankAccountToInsert.put("account_number", bankAcc.getAccount_number());
				BankInfoCollection.insert(objBankAccountToInsert);
		    	
				respEntity = new ResponseEntity<BankInfo>(bankAcc, HttpStatus.CREATED);
			} else {
				respEntity = new ResponseEntity<BankInfo>(bankAcc, HttpStatus.NO_CONTENT);
			}
		}
    	return respEntity;
    }

    
    @RequestMapping(value="/API/V1/Users/{user_id}/BankInfo", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<List<BankInfo>> listAllBankAcc(@PathVariable String user_id) throws UnknownHostException, MongoException {
    	List<BankInfo> bankAccList = new ArrayList<BankInfo>();
    	ResponseEntity<List<BankInfo>> respEntity = null;
    	BankInfo bankAccount = null;
    	DBCollection BankInfoCollection = UserController.getCollection("BankInfo");
    	DBObject objQuery;
		DBCursor cursor;
    	
    	objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).get();
		cursor = BankInfoCollection.find(objQuery);
		if(cursor.hasNext()) {
			while(cursor.hasNext()) {
				objQuery = cursor.next();
				bankAccount = new BankInfo();
				
				bankAccount.setBa_id(objQuery.get("ba_id").toString());
				bankAccount.setAccount_name(objQuery.get("account_name").toString());
				bankAccount.setRouting_number(objQuery.get("routing_number").toString());
				bankAccount.setAccount_number(objQuery.get("account_number").toString());
				
				bankAccList.add(bankAccount);
			}
			respEntity = new ResponseEntity<List<BankInfo>>(bankAccList, HttpStatus.OK);
		} else {
			respEntity = new ResponseEntity<List<BankInfo>>(bankAccList, HttpStatus.NO_CONTENT);
		}
    	return respEntity;
    }
    
   

	@RequestMapping(value="/API/V1/Users/{user_id}/BankInfo/{ba_id}", method=RequestMethod.DELETE)
    public ResponseEntity<BankInfo> deleteBankAcc(@PathVariable String user_id, @PathVariable String ba_id) throws UnknownHostException, MongoException {
		DBCollection BankInfoCollection = UserController.getCollection("BankInfo");
    	DBObject objQuery;
		DBCursor cursor;
		BankInfo bankInfo = null;
    	
    	objQuery = BasicDBObjectBuilder.start().add("user_id", user_id).add("ba_id", ba_id).get();
		cursor = BankInfoCollection.find(objQuery);
		if (cursor.hasNext()) {
			objQuery = cursor.next();
			BankInfoCollection.remove(objQuery);
		}

		return new ResponseEntity<BankInfo>(bankInfo, HttpStatus.NO_CONTENT); 
    }


}

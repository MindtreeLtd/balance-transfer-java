/*
 *  Copyright 2018, Mindtree Ltd. - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package blockchain.controller;

import java.util.Date;
import org.hyperledger.fabric.sdk.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import blockchain.dto.FunctionAndArgsDto;
import blockchain.dto.ChaincodeNameDto;
import blockchain.dto.UserDto;
import blockchain.filter.JwtFilter;
import blockchain.service.ChaincodeService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@RestController
public class ChaincodeController {

	private static final Logger logger = LoggerFactory.getLogger(ChaincodeController.class);
	 private static final long EXPIRATIONTIME = 900000;

	@Autowired
	ChaincodeService chaincodeService;

	/**
	 * Return the status as the particular user is enrolled
	 * 
	 * @param Status
	 *            of the user registered and enrolled in blockchain.
	 * @return the status as string
	 */

	@RequestMapping(value = "/enroll", method = RequestMethod.POST)
	public ResponseEntity<String> enroll(@RequestBody UserDto user) {

		String result = chaincodeService.enrollAndRegister(user.getUsername());
		if (result != "Failed to enroll user") {

			String jwtToken = "";

			if (user.getUsername() == null) {
				return ResponseEntity
						.status(HttpStatus.FORBIDDEN)
						.body("please enter username in request body");
			}

			String username = user.getUsername();
            
			jwtToken = Jwts.builder().setSubject(username).claim("roles", "user").setIssuedAt(new Date())
					.signWith(SignatureAlgorithm.HS256, "secretkey").setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME)).compact();
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(result + "  jwt:" + jwtToken);

		}

		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body("Something went wrong");

	}

	/**
	 * Return the channel that has been created , it takes the JWT token of the
	 * user that is constructing it.
	 * 
	 * @param Authorization
	 * @returns the channel that has been created
	 */
	@RequestMapping(value = "/api/construct", method = RequestMethod.POST)
	public ResponseEntity<String> createChannel(@RequestHeader String Authorization) throws Exception {
		String uname = JwtFilter.uname;
		logger.debug(uname);
		if(uname!=null)
		{
		String result = chaincodeService.enrollAndRegister(uname);
		if (result != "Failed to enroll user") {
			String response=chaincodeService.constructChannel();
			if(response=="Channel created successfully")
			{
				return ResponseEntity
						.status(HttpStatus.OK)

						.body("channel created successfully");
			}
			else
			{
				return ResponseEntity
						.status(HttpStatus.FORBIDDEN)
						.body("Something went wrong");
			}

		} else {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Something went wrong");
		}
		}
		else
		{
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Invalid JWT Token");
			
		}
	}

	/**
	 * 
	 * Return the channel that has been recreated , it takes the JWT token of
	 * the user that is constructing it.
	 * 
	 * @param Authorization
	 * @returns the channel that has been recreated
	 */
	@RequestMapping(value = "/api/reconstruct", method = RequestMethod.PUT)
	public ResponseEntity<String> recreateChannel(@RequestHeader String Authorization) throws Exception {

		String uname = JwtFilter.uname;
		if(uname!=null)
		{

		String result = chaincodeService.enrollAndRegister(uname);
		if (result != "Failed to enroll user") {
			Channel ch =chaincodeService.reconstructChannel();
			if(ch==null)
			{
				return ResponseEntity
						.status(HttpStatus.FORBIDDEN)
						.body("Channel recreation failed");
			}
			return ResponseEntity
					.status(HttpStatus.OK)
					.body("Channel recreated successfully");
		} else {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Something went wrong");
		}
		}		
		else
		{
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Invalid JWT Token");
			
		}
	}

	/**
	 * takes as input chaincode name and authorization token and returns status
	 * message as string for installation of chaincode.
	 * 
	 * @param chaincodeName
	 * @param Authorization
	 * @return the status as string
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/install", method = RequestMethod.POST)
	public ResponseEntity<String> installChaincode(@RequestBody ChaincodeNameDto chaincodeName, @RequestHeader String Authorization)
			throws Exception {
		String uname = JwtFilter.uname;
		if(uname!=null)
		{
		String result = chaincodeService.enrollAndRegister(uname);
		if (result != "Failed to enroll user") {
			String response=chaincodeService.installChaincode(chaincodeName.getChaincodeName());
			if(response=="Chaincode installed successfully")
			{
				return ResponseEntity
						.status(HttpStatus.OK)
						.body(response);
			}
			else {
				return ResponseEntity
						.status(HttpStatus.FORBIDDEN)
						.body(response);
			}
		} else {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Something went wrong");
		}
		
		}
		else
		{
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Invalid JWT Token");
			
		}
		
	}

	/**
	 * takes input as function name (init), arguments , chaincode name and
	 * authorization token.
	 * 
	 * @param Authorization
	 * @return status as string
	 */
	@RequestMapping(value = "/api/instantiate", method = RequestMethod.POST)
	public ResponseEntity<String> instantiateChaincode(@RequestBody FunctionAndArgsDto chaincodeDto,
			@RequestHeader String Authorization) throws Exception {

		if ((chaincodeDto.getFunction()) == null) {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("function not present in method body");
		}
		if (chaincodeDto.getArgs() == null) {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("args not present in method body");
		}
		String uname = JwtFilter.uname;
		if(uname!=null)
		{
		String result = chaincodeService.enrollAndRegister(uname);
		if (result != "Failed to enroll user") {
			String response=chaincodeService.instantiateChaincode(chaincodeDto.getChaincodeName(), chaincodeDto.getFunction(),
					chaincodeDto.getArgs());
			if(response=="Chaincode instantiated Successfully")
			{
				return ResponseEntity

						.status(HttpStatus.OK)
						.body(response);
			}
			else {
				return ResponseEntity
						.status(HttpStatus.FORBIDDEN)
						.body(response);
			}


		} else {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Something went wrong");
		}
		}
		else
		{
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Invalid JWT Token");
			
		}
	}

	/**
	 * takes input as function name (invoke), arguments , chaincode name and
	 * authorization token.
	 * 
	 * @param Authorization
	 * @return status as string
	 */
	@RequestMapping(value = "/api/invoke", method = RequestMethod.POST)
	public ResponseEntity<String> invokeChaincode(@RequestBody FunctionAndArgsDto chaincodeDto, @RequestHeader String Authorization)
			throws Exception {
		if ((chaincodeDto.getFunction()) == null) {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("function not present in method body");
		}
		if (chaincodeDto.getArgs() == null) {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("args not present in method body");
		}
		String uname = JwtFilter.uname;
		if(uname!=null)
		{
		String result = chaincodeService.enrollAndRegister(uname);
		if (result != "Failed to enroll user") {
			String response=chaincodeService.invokeChaincode(chaincodeDto.getChaincodeName(), chaincodeDto.getFunction(),
					chaincodeDto.getArgs());
			if(response=="Transaction invoked successfully")
			{
				return ResponseEntity
						.status(HttpStatus.OK)
						.body(response);
			}
			else
			{
				return ResponseEntity
						.status(HttpStatus.FORBIDDEN)
						.body(response);
			}
		} else {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Something went wrong");
		}
		}
		else
		{
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Invalide JWT token");
			
		}
	}

	/**
	 * 
	 * @param ChaincodeName
	 * @param ChaincodeFunction
	 * @param ChaincodeArgs
	 * @param Authorization
	 * @return payload returned from the chaincode
	 */
	@RequestMapping(value = "/api/query", method = RequestMethod.GET)
	public ResponseEntity<String> queryChaincode(@RequestParam("ChaincodeName") String ChaincodeName,
			@RequestParam("function") String ChaincodeFunction, @RequestParam("args") String[] ChaincodeArgs,
			@RequestHeader String Authorization) throws Exception {
		String uname = JwtFilter.uname;
		if(uname!=null)
		{
		String result = chaincodeService.enrollAndRegister(uname);

		if (result != "Failed to enroll user") {

			String response = chaincodeService.queryChaincode(ChaincodeName, ChaincodeFunction, ChaincodeArgs);
			if(response !="Caught an exception while quering chaincode")
			{
				return ResponseEntity
						.status(HttpStatus.OK)
						.body(response);
			}
			else
			{
				return ResponseEntity
						.status(HttpStatus.FORBIDDEN)
						.body(response);
			}

		} else {
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Something went wrong");
		}
		}
		else
		{
			return ResponseEntity
					.status(HttpStatus.FORBIDDEN)
					.body("Invalide JWT token");
			
		}
	}

}

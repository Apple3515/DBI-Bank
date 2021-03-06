package com.bank.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;

import com.bank.model.pojo.Customer;
import com.bank.model.pojo.TransactionDetails;

@Controller
public class TransactionController
{
	
	@Autowired
	RestTemplate restTemplate;
	
	static String url = "http://localhost:5050/";

	@RequestMapping("deposit")
	public String Deposite(@SessionAttribute("Id") String id)
	{
		if (id!=null)
		{
			
			return "Deposit";		
		}
		else
			return "Home";
	}
	
	@RequestMapping("depositeAmmount")
	public String depositeAmmount(@RequestParam("amount") double amt,@SessionAttribute("Id") int id,Model m)
	{
		LocalDateTime now = LocalDateTime.now();
		System.out.println(now);
		String s = now.getYear()+""+now.getMonthValue()+now.getDayOfMonth()+now.getHour()+""+now.getMinute()+now.getSecond();
		System.out.println(s);
		Customer customer = restTemplate.getForObject(url + "getCustomer/"+id, Customer.class);
		double bal = customer.getBalance();
		customer.setBalance(bal+amt);
		restTemplate.exchange(url+"updateUserBalance", HttpMethod.PUT, new HttpEntity<>(customer), String.class).getBody();
		TransactionDetails details = new TransactionDetails();
		details.setBalance(bal+amt);
		details.setId(id);
		details.setCredit(amt);
		details.setTransId(s);
		String date = now.getDayOfMonth()+"-"+now.getMonthValue()+"-"+now.getYear();
		details.setDate(date);
		String st = restTemplate.postForObject(url+"insertTransaction",details, String.class);
		System.out.println(st);
		m.addAttribute("bal", customer.getBalance());
		details.setId(customer.getId());
		
		
		return "viewBalance";
	}
	
	@RequestMapping("withdraw")
	public String withd(@SessionAttribute("Id") String id)
	{
		if (id!=null)
			return "withdraw";
		else
			return "Home";
	}
	
	@RequestMapping("withdrawAmmount")
	public String withdrawAmmount(@RequestParam("amount") double amt,@SessionAttribute("Id") int id,Model m)
	{
		Customer customer = restTemplate.getForObject(url + "getCustomer/"+id, Customer.class);
		double bal = customer.getBalance();
		if(amt<=0)
		{
			m.addAttribute("msg", "Invalid Input");
			return "withdraw";
		}
		else if(amt>bal)
		{
			m.addAttribute("msg", "Insufficient Balance");
			return "withdraw";
		}
		else
		{
			LocalDateTime now = LocalDateTime.now();
			String s = now.getYear()+""+now.getMonthValue()+now.getDayOfMonth()+now.getHour()+""+now.getMinute()+now.getSecond();
			customer.setBalance(bal-amt);
			restTemplate.exchange(url+"updateUserBalance", HttpMethod.PUT, new HttpEntity<>(customer), String.class).getBody();
			TransactionDetails details = new TransactionDetails();
			details.setBalance(bal-amt);
			details.setId(id);
			details.setDebit(amt);
			details.setTransId(s);
			String date = now.getDayOfMonth()+"-"+now.getMonthValue()+"-"+now.getYear();
			details.setDate(date);
			String st = restTemplate.postForObject(url+"insertTransaction",details, String.class);
			System.out.println(st);
			m.addAttribute("bal", customer.getBalance());
			return "viewBalance";
		}
	}

	@RequestMapping("balanceview")
	public String bal(@SessionAttribute("Id") String id,Model m)
	{
		if(id!=null)
		{
			Customer customer = restTemplate.getForObject(url + "getCustomer/"+id, Customer.class);
			m.addAttribute("bal", customer.getBalance());
			return "viewBalance";
		}
		else
			return "Home";
	}
	
	@RequestMapping("fundtransfer")
	public String tx(@SessionAttribute("Id") String id,Model m)
	{
		if(id!=null)
		{
			Customer customer = restTemplate.getForObject(url + "getCustomer/"+id, Customer.class);
			m.addAttribute("accNo", customer.getAccountNo());
			return "Transfer";
		}
		else
			return "Home";
	}
	
	@RequestMapping("toTransfer")
	public String toTransfer(@SessionAttribute("Id") int id,Model m,@RequestParam("toAccno")int toAccno,@RequestParam("transferAmm")double transferAmm)
	{
		Customer customer = restTemplate.getForObject(url + "getCustomer/"+id, Customer.class);
		double custBal = customer.getBalance();
		Customer toCustomer = restTemplate.getForObject(url + "getToCustomer/"+toAccno, Customer.class);
		if (toCustomer!=null)
		{
			double toBal = toCustomer.getBalance();
			if(custBal>transferAmm)
			{
				toCustomer.setBalance(toBal+transferAmm);
				customer.setBalance(custBal-transferAmm);
				restTemplate.exchange(url+"updateUserBalance", HttpMethod.PUT, new HttpEntity<>(toCustomer), String.class).getBody();
				restTemplate.exchange(url+"updateUserBalance", HttpMethod.PUT, new HttpEntity<>(customer), String.class).getBody();
				
				LocalDateTime now = LocalDateTime.now();
				String s = now.getYear()+""+now.getMonthValue()+now.getDayOfMonth()+now.getHour()+""+now.getMinute()+now.getSecond();
				TransactionDetails debittr = new TransactionDetails();
				debittr.setBalance(custBal-transferAmm);
				debittr.setId(id);
				debittr.setDebit(transferAmm);
				debittr.setTransId(s);
				String date = now.getDayOfMonth()+"-"+now.getMonthValue()+"-"+now.getYear();
				debittr.setDate(date);
				String st = restTemplate.postForObject(url+"insertTransaction",debittr, String.class);
				
				String s2 = now.getYear()+""+now.getMonthValue()+now.getDayOfMonth()+now.getHour()+""+now.getMinute()+now.getSecond();
				TransactionDetails credittr = new TransactionDetails();
				credittr.setBalance(custBal+transferAmm);
				credittr.setId(toCustomer.getId());
				credittr.setCredit(transferAmm);
				credittr.setTransId(s);
				credittr.setDate(date);
				String st2 = restTemplate.postForObject(url+"insertTransaction",credittr, String.class);
				m.addAttribute("msg", "Success");
				
				return "Transfer";
			}
			else
			{
				m.addAttribute("msg", "Insufficient Balance");
				return "Transfer";
			}
		}
		else
		{
			m.addAttribute("msg", "Please Enter Valid Account Number");
			return "Transfer";
		}
	}
	
	@RequestMapping("info")
	public String info(@SessionAttribute("Id") String id,Model m)
	{
		if (id!=null)
		{
			Customer customer = restTemplate.getForObject(url + "getCustomer/"+id, Customer.class);
			m.addAttribute("user", customer);
			return "info";
		}
		else
			return "Home";
	}
}
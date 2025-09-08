# 1. Component Change






# 2. How to run and test 
## How to run
1. install and run postgres database
2. fill database username and password inapplication.properties


## How to test
1. Create a customer with following endpoint and payload
```json
POST http://localhost:YOUR_PORT/api/customer
{
   "firstName": "5348",
   "lastName": "5348"
}

{
    "firstName": "bank",
    "lastName": "bank"
}
```

2. Save CUSTOMER_ID, BANK_CUSTOMER_ID from above request, and create two personal account, and one business account, and one revenue account with bank_CUSTOMER_id
```json
1. POST http://localhost:YOUR_PORT/api/customer/{CUSTOMER_ID}/account
{
"accountName":"personal 1",
"accountType": "PERSONAL"
}

{
"accountName":"personal 2",
"accountType": "PERSONAL"
}

{
"accountName":"business",
"accountType": "BUSINESS"
}

{
"accountName":"revenue",
"accountType": "REVENUE"
}
```

- Save all the accountID with the relative customer id
- !***Update REVENUE_ACCOUNT_ID in TransactionRecordService.java after creation of revenue account***

3. deposit  100 dollar in to personal 1 account 
```json
http://{{LOCAL_HOST}}/api/customer/{customerid}/account/{accountid}/transaction_record/deposit
{
  "amount": 1000
}

```

4.perform one transaction to business, check merchant fee is applied and transfer to revenue account
```json
http://{{LOCAL_HOST}}/api/customer/{customerid}/account/{accountid}/transaction_record/transfer

{
    "toCustomerId":2, //BUSINESS CUSTOMER ID
    "toAccountId":3, // BUSINESS Account id
    "amount": 100
}

- you can either run databse to verify merchant fee in account table and transaction_record table
- plus, you can check terminal log

```

5. check transfer from personal to personal, merchant fee won't applied
```json
http://{{LOCAL_HOST}}/api/customer/{customerid}/account/{accountid}/transaction_record/transfer

{
    "toCustomerId":2, //PERSONAL customer ID
    "toAccountId":2, //PERSONAL account id 
    "amount": 100
}
```
- check terminal log, 
- then check balance is updated in database from account and transaction_record table
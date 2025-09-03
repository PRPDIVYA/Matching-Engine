# Matching-Engine
Exchange Matching-engine spring boot application implementing a limit order matching engine ( that matches people (or organizations) who want to buy an asset with people who want to sell an matching asset )
with Price/Timealgorithm (FIFO).
Application runs on port 8080.
Order Id starts from 0 as expected in the assement.
No Persistance is implemented . Orders will be reset after restart.

#API EndPoints :
-POST /orders : place a new limit order 
-GET /orders/{orderId} : Get the current status of order
PlaceOrder :

curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"asset":"BTC","price":43251.00,"amount":1.0,"direction":"SELL"}'

GetOrder: 
curl http://localhost:8080/orders/0

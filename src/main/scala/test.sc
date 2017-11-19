val txt = "{\"type\":\"ticker\",\"sequence\":4392902023,\"product_id\":\"BTC-USD\",\"price\":\"7812.00000000\",\"open_24h\":\"7643.00000000\",\"volume_24h\":\"10102.71105424\",\"low_24h\":\"7812.00000000\",\"high_24h\":\"7847.98000000\",\"volume_30d\":\"625869.28881853\",\"best_bid\":\"7811.99\",\"best_ask\":\"7812\",\"side\":\"buy\",\"time\":\"2017-11-19T04:03:41.992000Z\",\"trade_id\":24525825,\"last_size\":\"0.00000127\"}"
val keys = txt.replace("{","").replace("}","").split(",\"").map(_.split("\":")(0).replace("\"",""))
val values = txt.split(',').map(_.split("\":")(1))
val l1 = keys.length
val l2 = values.length



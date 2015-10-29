package com.yaojuncn.jedis

import java.util.concurrent.CountDownLatch

import redis.clients.jedis.{JedisPool, JedisPoolConfig}

import scala.collection.mutable.ListBuffer


object JedisPerfTest {

  val gtimelist = new ListBuffer[Long]()

  def main(args: Array[String]): Unit ={
    println("starting JedisPerfTest on jedis 2.7.3....")
    onecall // to init the pool

    test1


  }



  val pool = {
    val config = new JedisPoolConfig()
    config.setMinIdle(2)
    config.setMaxIdle(8)

    // below are for jedis 2.0.0
    // config.setMaxActive(8)
    // config.setMaxWait(20)


    // below are for jedis 2.7.3
    config.setMaxTotal(8)
    // config.setMaxWaitMillis(20)

    val pool: JedisPool = new JedisPool(config, "localhost", 6379, 2000)
    pool
  }

  def test1(): Unit ={
    runthreadtest

    for(item <- gtimelist){
      println(item)
    }

  }


  def runthreadtest(): Unit ={

    val starttime = System.currentTimeMillis()

    val tno = 16
    val start = new CountDownLatch(1)

    val round = 10000

    val finish = new CountDownLatch(tno)
    for(i <- 1 to tno){
      new Thread(new Runnable(){
        override def run() = {

          start.await()
          for(j <- 1 to round){
            onecall
          }

          finish.countDown()

        }
      }).start()
    }

    start.countDown()

    finish.await()


    val avgt = (System.currentTimeMillis() - starttime).toDouble/tno/round
    println("tno=" + tno + ", round=" + round + ", avgt(ms)=" + avgt)

  }




  def onecall(): Unit ={

    val start = System.currentTimeMillis()
    val jedis = pool.getResource
    val t = System.currentTimeMillis() - start
    if(t > 20){
      gtimelist.+=(t)
    }
    try{
      jedis.set("hello", "world")
      jedis.get("hello")
    }
    catch{
      case e: Exception=>e.printStackTrace()
    }
    finally{
      if(null != jedis){
        //jedis.close()
        pool.returnResource(jedis)
      }
    }
  }

}

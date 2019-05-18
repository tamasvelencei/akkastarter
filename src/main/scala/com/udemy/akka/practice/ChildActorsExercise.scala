package com.udemy.akka.practice

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.udemy.akka.practice.ChildActorsExercise.TestActor.Go
import com.udemy.akka.practice.ChildActorsExercise.WordCounterMaster.{Initialize, WordCountReply, WordCountTask}

import scala.math.Ordering.String

object ChildActorsExercise extends App{
	
	object WordCounterMaster{
		case class Initialize(int: Int)
		case class WordCountTask(id: Int, text: String)
		case class WordCountReply(id: Int, count: Int)
	}
	
	class WordCounterMaster extends Actor {
		
		override def receive: Receive = {
			case Initialize(nChildren) =>
				val childrenRefs = for (i <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker], s"Child_$i")
				context become delegateTask(0, 0, childrenRefs, Map[Int, ActorRef]())
		}
		
		def delegateTask(currentTaskId: Int, currentChildIndex: Int, childRefs: Seq[ActorRef], requestMap: Map[Int, ActorRef]): Receive = {
			case text: String =>
				val index = if (currentChildIndex == childRefs.size) 0 else currentChildIndex
				childRefs.apply(index) ! WordCountTask(currentTaskId, text)
				context become delegateTask(currentTaskId + 1, currentChildIndex + 1, childRefs, requestMap + (currentTaskId -> sender()))
			case WordCountReply(id, count) =>
				requestMap(id) ! s"Request resolved by: $sender() word count is: $count"
		}
	}
	
	class WordCounterWorker extends Actor {
		override def receive: Receive = {
			case WordCountTask(id, text) =>
				sender() ! WordCountReply(id, text.trim.split(" ").length)
		}
	}
	
	object TestActor {
		case class Go(actRef: ActorRef)
	}
	class TestActor extends Actor {
		override def receive: Receive = {
			case Go(actRef) =>
				actRef ! Initialize(4)
				actRef ! "Akka is awesome!"
				actRef ! "Akka is awesome !"
				actRef ! "Akka!"
				actRef ! "1 2 3 4 5!"
				actRef ! "1 2"
			case message: String => println(message)
		}
	}
	
	val system = ActorSystem("WorkWithChildren")
	val master = system.actorOf(Props[WordCounterMaster], "Master")
	val testActor = system.actorOf(Props[TestActor])
	testActor ! Go(master)

}

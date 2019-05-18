package com.udemy.akka.practice

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.udemy.akka.practice.StatelessExercise.Citizen.Vote
import com.udemy.akka.practice.StatelessExercise.VoteAggregator.AggregateVotes

object StatelessExercise extends App{

	object Counter {
		case object AccountSuspended
		case object AccountActivated
		case class Print(what: String = "")
		case class Increment(amount: Int)
		case class Decrement(amount: Int)
		val ACC_ACTIVE = "Account is active"
		val ACC_SUSPENDED = "Account is suspended"
	}
	
	class Counter extends Actor {
		import Counter._
		
		override def receive: Receive = active(0)
		
		def suspended(num: Int): Receive = {
			case Increment => sender() ! AccountSuspended
			case Decrement => sender() ! AccountSuspended
			case Print(what: String) =>
				if (what equals "status") println(ACC_SUSPENDED)
				else println(num)
			case AccountActivated => context.become(active(num))
		}
		
		def active(num: Int): Receive = {
			case Increment(amount) => context.become(active(num + amount))
			case Decrement(amount) => context.become(active(num - amount))
			case Print(what: String) =>
				if (what equals "status") println(ACC_ACTIVE)
				else println(num)
			case AccountSuspended => context.become(suspended(num))
		}
	}
	
	val system = ActorSystem("Udemy-Practice")
	val counter = system.actorOf(Props[Counter])

	import Counter._
	counter ! Print("")
	counter ! Print("status")
	counter ! Increment(10)
	counter ! Print("")
	counter ! AccountSuspended
	counter ! Print("status")
	counter ! Decrement(5)
	counter ! Print("")
	counter ! AccountActivated
	counter ! Print("status")
	counter ! Increment(10)
	counter ! Print("")
	
	object Citizen {
		case class Vote(candidate: String)
		case object VoteStatusRequest
		case class VoteStatusReply(candidate: Option[String])
	}
	
	class Citizen extends Actor {
		import Citizen._
		
		override def receive(): Receive = vote(None)
		
		def vote(voteFor: Option[String]): Receive = {
			case Vote(candidate) => context.become(vote(Some(candidate)))
			case VoteStatusRequest => sender() ! VoteStatusReply(voteFor)
		}
	}
	
	object VoteAggregator{
		case class AggregateVotes(citizens: Set[ActorRef])
		case object Print
	}
	
	class VoteAggregator extends Actor {
		import VoteAggregator._
		import Citizen._
		
		override def receive: Receive = countCandidates(Map())
		
		def countCandidates(map: Map[String, Int]): Receive = {
			case AggregateVotes(citizens) =>
				citizens.foreach( x => x ! VoteStatusRequest)
			case VoteStatusReply(None) =>
			case VoteStatusReply(Some(candidate)) =>
				println(map.updated(candidate, map.getOrElse(candidate, 0) + 1))
				context.become(countCandidates(map.updated(candidate, map.getOrElse(candidate, 0) + 1)))
		}
	}
	
	val alice = system.actorOf(Props[Citizen])
	val bob = system.actorOf(Props[Citizen])
	val charlie = system.actorOf(Props[Citizen])
	val daniel = system.actorOf(Props[Citizen])
	val voteAggregator = system.actorOf(Props[VoteAggregator])
	
	alice ! Vote("Martin")
	bob ! Vote("Jonas")
	charlie ! Vote("Roland")
	daniel ! Vote("Roland")
	
	voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))
	
	
}

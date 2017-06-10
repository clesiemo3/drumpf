package clesiemo3.drumpf

import twitter4j._

import java.io.File
import com.typesafe.config.{ Config, ConfigFactory }
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer


object Util {
  var dotEnv = ConfigFactory.empty()
  if (sys.env.get("CONSUMER_KEY")==None){
    dotEnv = ConfigFactory.parseFile(new File(".env"))
  }
  val config = new twitter4j.conf.ConfigurationBuilder()
    .setDebugEnabled(true)
    .setOAuthConsumerKey(sys.env.getOrElse("CONSUMER_KEY",dotEnv.getString("CONSUMER_KEY")))
    .setOAuthConsumerSecret(sys.env.getOrElse("CONSUMER_SECRET",dotEnv.getString("CONSUMER_SECRET")))
    .setOAuthAccessToken(sys.env.getOrElse("ACCESS_TOKEN",dotEnv.getString("ACCESS_TOKEN")))
    .setOAuthAccessTokenSecret(sys.env.getOrElse("ACCESS_SECRET",dotEnv.getString("ACCESS_SECRET")))
    .build
}

object FollowIdsStreamer {
  def getFullTimeline(timelineUser: String) = {
    val twitter = new TwitterFactory(Util.config).getInstance
    val pager = new Paging().count(200)
    val timeline = new ListBuffer[Any]
    var loop = true

    while(loop){
      val statuses = twitter.getUserTimeline(timelineUser, pager).asScala
      if (statuses.isEmpty) {
        loop = false
      } else {
        for (status <- statuses) {
          //println("@" + status.getUser.getScreenName + " - " + status.getText + " - " + status.getCreatedAt)
          //println("%s %s".format(status.getText, status.getCreatedAt))
          timeline.append(List(status.getId, status.getText, status.getCreatedAt))
        }
        //set pager with new maxId
        pager.setMaxId(statuses.last.getId() - 1)
      }
    }
    timeline
  }

  def main(args: Array[String]) {
    try { // gets Twitter instance with default credentials
      val x = getFullTimeline("realDonaldTrump")
    } catch {
      case te: TwitterException =>
        te.printStackTrace()
        println("Failed to get timeline: " + te.getMessage)
        System.exit(-1)
    }
  }
}
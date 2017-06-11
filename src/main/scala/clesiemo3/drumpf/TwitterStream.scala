package clesiemo3.drumpf

import twitter4j._
import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import com.github.tototoshi.csv._

object Util {
  def writeCSV(l: List[List[Any]], fileName: String) : Unit = {
    val f = new File(fileName)
    val writer = CSVWriter.open(f)
    writer.writeAll(l)
    writer.close()
  }

  var dotEnv: Config = ConfigFactory.empty()
  if (sys.env.get("CONSUMER_KEY").isEmpty){
    dotEnv = ConfigFactory.parseFile(new File(".env"))
  }
  val config: twitter4j.conf.Configuration = new twitter4j.conf.ConfigurationBuilder()
    .setDebugEnabled(true)
    .setOAuthConsumerKey(sys.env.getOrElse("CONSUMER_KEY",dotEnv.getString("CONSUMER_KEY")))
    .setOAuthConsumerSecret(sys.env.getOrElse("CONSUMER_SECRET",dotEnv.getString("CONSUMER_SECRET")))
    .setOAuthAccessToken(sys.env.getOrElse("ACCESS_TOKEN",dotEnv.getString("ACCESS_TOKEN")))
    .setOAuthAccessTokenSecret(sys.env.getOrElse("ACCESS_SECRET",dotEnv.getString("ACCESS_SECRET")))
    .build
}

object Timeline {
  def getFullTimeline(timelineUser: String) : List[List[Any]] = {
    val twitter = new TwitterFactory(Util.config).getInstance
    val pager = new Paging().count(200)
    val timeline = new ListBuffer[List[Any]]
    var loop = true

    while(loop){
      val statuses = twitter.getUserTimeline(timelineUser, pager).asScala
      if (statuses.isEmpty) {
        loop = false
      } else {
        for (status <- statuses) {
         timeline.append(List(status.getId, status.getText, status.getCreatedAt))
        }
        //set pager with new maxId
        pager.setMaxId(statuses.last.getId - 1)
      }
    }
    val timelineOut = timeline.toList
    timelineOut
  }

  def saveTimeline(timelineUser: String, fileType: String) : Unit = {
    val t = getFullTimeline(timelineUser)
    fileType.toLowerCase() match {
      case "csv" => Util.writeCSV(t, "timeline.csv")
      //TODO: Database connectivity
      case "db" => None
    }

  }

  def main(args: Array[String]) {
    try {
      saveTimeline("realDonaldTrump","csv")
    } catch {
      case te: TwitterException =>
        te.printStackTrace()
        println("Failed to get timeline: " + te.getMessage)
        System.exit(-1)
    }
  }
}
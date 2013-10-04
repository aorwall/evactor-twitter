Evactor Twitter example
====================
This is an example implementation of Evactor that analyses status updates on 
Twitter. It receives tweets from the [Twitter Streaming API](https://dev.twitter.com/docs/streaming-api). 
The hashtags are the extracted from the tweets and processors in Evactor keep a
count on how many times each hashtag occurred for the last 15 minutes. When the 
count changes, an event with the current count is published to a channel. 

To visualize this a web page is provided that receives all new events. A demo
of this page is found here: 

By following the instructions below you should be able to deploy this application yourself.


Run the application
---------------------

Install [sbt](https://github.com/harrah/xsbt/wiki/Getting-Started-Setup) if you don't already have it. The application has been tested with sbt 0.11.3.

Clone the Evactor repo: `git clone git://github.com/aorwall/evactor-twitter.git`

Go to https://dev.twitter.com/apps and create a new application. Then use the credentials provided there in `src/main/resources/twitter4j.properties`.

Compile the code: `sbt compile`

Generate start script: `sbt start-script`

Start the application with the start script: `target/start`

Go to http://localhost:8080 to see the app in action.


Understand the configuration
---------------------
All the components that runs on Evactor can be configured in the configuration file ([application.conf](https://github.com/aorwall/evactor/blob/master/example/src/main/resources/application.conf)). Therefore it's a good start to examine the example configuration to get an understanding on how it all hangs together.

This example has two main flows, one for finding trending hashtags and one to look for popular URL:s.

### Collecting events
To get events into the Evactor event stream we need a [*collector*](https://github.com/aorwall/evactor/blob/master/core/src/main/scala/org/evactor/collect/Collector.scala). The collector is connected to a *listener*, used to receive events from external sources, and a *transformer* that transforms events in external formats to an internal event object. In this example, the collector receives status updates from Twitter's 'Spritzer' API (1% of all status updates on Twitter) and publishes them to the channel `twitter`. 

#### Collector flow
1. A custom [listener class](https://github.com/aorwall/evactor/blob/master/example/src/main/scala/org/evactor/twitter/listener/TwitterListener.scala) listens for new tweets on `https://stream.twitter.com/1/statuses/sample.json`. 
2. When the listener receives a tweet, in json format, it sends the tweet to a custom [transformer](https://github.com/aorwall/evactor/blob/master/example/src/main/scala/org/evactor/twitter/transformer/TwitterJsonToStatusEvent.scala) that transforms the Json message to a [StatusEvent](https://github.com/aorwall/evactor/blob/master/example/src/main/scala/org/evactor/twitter/StatusEvent.scala).
3. The transformers send the message to the collector that checks for duplicates and publishes the StatusEvent to the channel `twitter`.

#### The configuration
> To use the Twitter Stream API, go to https://dev.twitter.com/apps and create a new application. Then use the credentials provided there in `src/main/resources/twitter4j.properties`.

```text
twitter_collector {
  listener {
    class = "org.evactor.twitter.listener.TwitterListener"
  }
  transformer {
    class = "org.evactor.twitter.transformer.TwitterJsonToStatusEvent"
  }
  publication = { channel = "twitter" }
}
```

### Popular hashtags
Look for popular hashtags by analysing how many times events with a specific hashtag occured in a 15 minutes time.

#### Filter 
A [*Filter*](https://github.com/aorwall/evactor/blob/master/core/src/main/scala/org/evactor/process/route/Filter.scala) subscribes to `twitter` and filters out    all status updates not containing hashtags and publishes the rest to the channel `twitter:hashtag` categorized by hashtag.

[mvel](http://mvel.codehaus.org/) is used to examine the content of the event.

`hashtags == null || hashtags.size() == 0` declares that the hashtag list in the StatusEvent object is null or doesn't contain any elements, *true* is returned and the Filter will not forward the event.

In *publication* the mvel expression `hashtags` declares that the event category on the event will be set to all hashtags specified in the event.

`accept = false` specifies that the filter should filter out all events that corresponds with the expression.

```text
twitter_hashtag_filter {
  type = filter 
  subscriptions = [ {channel = "twitter"} ]
  publication = { channel = "twitter:hashtag" }
  expression = { mvel = "hashtags == null || hashtags.size() == 0" } 
  accept = false
}
```

#### Count analyser

```text
twitter_hashtag_counter {
  type = countAnalyser
  subscriptions = [ { channel = "twitter:hashtag" } ]
  publication = { channel = "twitter:hashtag:count" }
  categorization = { OneAndOne { mvel = "hashtags" } }
  timeframe = 15 minutes
}
```   


Licence
---------------------
Copyright 2012 Albert Örwall

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0

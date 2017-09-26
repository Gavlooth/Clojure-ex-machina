(ns analytics.random-forest
  (:import [org.apache.spark.SparkConf]
           [org.apache.spark.api.java.JavaPairRDD]
           [org.apache.spark.api.java.JavaRDD]
           [org.apache.spark.api.java.JavaSparkContext]
           [org.apache.spark.mllib.regression.LabeledPoint]
           [org.apache.spark.mllib.tree.RandomForest]
           [org.apache.spark.mllib.tree.model.RandomForestModel]
           [org.apache.spark.mllib.util.MLUtils]))


;https://github.com/apache/spark/blob/master/examples/src/main/java/org/apache/spark/examples/mllib/JavaRandomForestClassificationExample.java

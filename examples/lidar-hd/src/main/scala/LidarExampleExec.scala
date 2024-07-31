/**
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
    *
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
* limitations under the License.
  */


import Main.resourceFolder
import org.apache.log4j.{Level, Logger}
import org.apache.sedona.spark.SedonaContext
import org.apache.sedona.spark.SedonaContext
import org.apache.sedona.viz.core.Serde.SedonaVizKryoRegistrator
import org.apache.sedona.viz.sql.utils.SedonaVizRegistrator
import org.apache.sedona.core.enums.{GridType, IndexType}
import org.apache.sedona.viz.core.{ImageGenerator, ImageSerializableWrapper, RasterOverlayOperator}
import org.apache.sedona.viz.extension.visualizationEffect.{ChoroplethMap, HeatMap, ScatterPlot}
import org.apache.sedona.viz.utils.ImageType
import org.apache.sedona.core.spatialRDD.{PolygonRDD, RectangleRDD}
import org.apache.sedona.core.spatialRDD.{PolygonRDD, RectangleRDD}
import org.apache.sedona.sql.utils.Adapter
import org.apache.sedona.core.spatialOperator.{JoinQuery, KNNQuery, RangeQuery}
import org.apache.spark.sql.SparkSession

import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;


import java.awt.Color


object LidarExampleExec {

  val demoOutputPath = "target/demo"

  def vizLidar(sedona: SparkSession): Boolean = {

    val LidarDf = sedona.read.parquet(resourceFolder + "LidarHD.parquet")

    LidarDf.createOrReplaceTempView("Lidardf")
    var LidarRDD = Adapter.toSpatialRdd(sedona.sql("select ST_PointZ(Lidardf.X,Lidardf.Y,Lidardf.Z) as point from Lidardf")
      , "point")

    LidarRDD.analyze()
    LidarRDD.spatialPartitioning(GridType.KDBTREE)
    LidarRDD.buildIndex(
      IndexType.RTREE,
      true
    )

    val env = LidarRDD.boundaryEnvelope
    val geometryFactory = new GeometryFactory()

    val K = 50

    // Example 1: very close points
    val pointObject1 = geometryFactory.createPoint(new Coordinate(271, 231,-0.002))
    val pointObject2 = geometryFactory.createPoint(new Coordinate(271, 231,0.002))
    val result1 = KNNQuery.SpatialKnnQuery(LidarRDD, pointObject1, K, false)
    val result2 = KNNQuery.SpatialKnnQuery(LidarRDD, pointObject2, K, false)
    println("Example 1: Very close centers")
    println(result1 == result2)

    // Example 2: separate points
    val pointObject3 = geometryFactory.createPoint(new Coordinate(271, 231,-100))
    val pointObject4 = geometryFactory.createPoint(new Coordinate(271, 231,100))
    val result3 = KNNQuery.SpatialKnnQuery(LidarRDD, pointObject3, K, false)
    val result4 = KNNQuery.SpatialKnnQuery(LidarRDD, pointObject4, K, false)
    println("Example 2: Far-away centers")
    println(result3 == result4)

    /*
    val imageResolutionX = 1000
    val imageResolutionY = 1000
    val frontImage = new ScatterPlot(imageResolutionX, imageResolutionY, LidarRDD.boundary, true)

    frontImage.CustomizeColor(0, 0, 0, 255, Color.GREEN, true)
    frontImage.Visualize(sedona.sparkContext, LidarRDD)

    //  frontImage.CustomizeColor(0, 0, 0, 255, null, false)
    //  val colorizeFunc = new Colorize[LidarRDD](LidarRDD) {
    //    override def fillColor(point: LidarRDD): Color = {
    //    val zValue = point.getUserData.asInstanceOf[Double] // Assumes Z value is stored in UserData
    //        val color = Color.getHSBColor((zValue / 255).toFloat, 1.0f, 1.0f) // Example mapping
    //  	      new Color(color.getRed, color.getGreen, color.getBlue, 255)
    //      }
    //   }
    // x  frontImage.setColorize(colorizeFunc)


    val backImage = new HeatMap(imageResolutionX, imageResolutionY, env, true, 1)
    backImage.Visualize(sedona.sparkContext, LidarRDD)
    val overlayOperator = new RasterOverlayOperator(backImage.rasterImage)
    overlayOperator.JoinImage(frontImage.rasterImage)

    val imageGenerator = new ImageGenerator
    imageGenerator.SaveRasterImageAsLocalFile(overlayOperator.backRasterImage,System.getProperty("user.dir"), ImageType.PNG)
    */
    true
  }
}


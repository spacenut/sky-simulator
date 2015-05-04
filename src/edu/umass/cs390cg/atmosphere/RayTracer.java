package edu.umass.cs390cg.atmosphere;

import edu.umass.cs390cg.atmosphere.geom.HitRecord;
import edu.umass.cs390cg.atmosphere.geom.Ray;
import edu.umass.cs390cg.atmosphere.scene.Scene;
import edu.umass.cs390cg.atmosphere.scene.SceneReader;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RayTracer {

  public static final RayTracer r = new RayTracer();

  public Scene scene;

  public static void main(String[] args) {
    if (args.length == 1) {
      r.scene = new SceneReader(args[0]).readScene();
      r.startTracing();
    } else {
      System.out.println("Usage: java RayTracer input.scene");
    }
  }

  public void startTracing() {
    int i,j;

    scene.image = new Vector3d[scene.width][scene.height];
    for(i=0;i<scene.width;i++)
      for(j=0;j<scene.height;j++)
        scene.image[i][j] = new Vector3d();

    PixelThread.dest = scene.image;
    PixelThread.width = scene.width;
    PixelThread.height = scene.height;
    PixelThread.camera = scene.camera;
    PixelThread.tracer = this;
    PixelThread.samples = 1;
    List<Thread> threads = new ArrayList<Thread>();
    double timeAtStart = System.nanoTime()/1e9d;

    //region Threaded

    for(int y = 0; y < scene.height; y++){
      Thread newThread = new Thread(new PixelThread(y));
      newThread.start();
      threads.add(newThread);
    }
    for(Thread t : threads){
      try {
        t.join();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }//*/
    //endregion

    //region Non threaded

  /*
    float x, y;
    for (j = 0; j < scene.height; j++) {
      y = (float) j / (float) scene.height;
      //System.out.print("\rray tracing... " + j * 100 / scene.height + "%");
      for (i = 0; i < scene.width; i++) {
        x = (float) i / (float) scene.width;
        scene.image[i][j] = trace(scene.camera.getCameraRay(x, y));
      }
    }//*/
    //endregion


    double timeAtEnd = System.nanoTime()/1e9d;
    System.out.println("Took " + (timeAtEnd-timeAtStart) + " seconds");

    System.out.println("\rray tracing completed.");
    scene.writeImage();
  }

  public Vector3d trace(Ray ray) {
    /*float d2 = -ray.d.dot(scene.sun.d);
    d2=(float)Math.pow(d2,14);
    return new Color3f(d2,d2,d2);*/
    HitRecord hit = scene.intersectScene(ray);
    if(hit == null) return new Vector3d();
    else if(hit.type == HitRecord.HitType.TYPE_SKY) {

      //return ScatteringEquations.cosOfVectorsNormalized(ray.d, scene.sun.d);
      //return
      return scene.sky.calculateShading(ray, hit);
    }
    else {

      Ray newRay = new Ray(hit.pos, scene.sun.d);
      HitRecord newHit = scene.intersectScene(newRay);

      if(newHit.type == HitRecord.HitType.TYPE_TERRAIN) {
        return new Vector3d();
      }

      Random r = new Random();
      double d = r.nextDouble() / 2 + .5;
      Vector3d x = scene.terrain.color;
      return new Vector3d(x.x*d,x.y*d,x.z*d);
    }
  }
}
##  Random terrain generator
- fault.java accepts as inputs w,h,t,k
- The goal here is to use multiple threads efficiently to generate a terrain using the fault-line approach.
Define an integer w x h grid of height values for a given an input w and h, all heights initialized to 0.
Once initialized, use t threads to modify height values: each thread chooses random entry/exit points
to define a fault-line and a random height adjustment (within a [0; 10] range), and then adds the height
adjustment value to every point on one side of the line. Each thread does this as fast as possible, with the
whole simulation ending when a total of k fault-lines have been created.
- Once all t threads have completed, the time taken in milliseconds should be emitted as console output and the image output to a file, named
outputimage.png.
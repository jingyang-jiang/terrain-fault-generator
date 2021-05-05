
package assignment1;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.*;



public class fault {

    // unshared Parameters  
	//  n = number of threads, k = number of faults
    public static int n;
    public static int width;
    public static int height;
    public static Grid grid;
    public static final Random rand = new Random();
    private static final List<FaultGenerator> aFaultGenerators= new ArrayList<>();
    private static int initialK;
    // Shared Parameters, protected by volatile
    public static volatile int k;
    
    public static void main(String[] args) {
        try {

            //reading arguments
            if (args.length>0) {
            	width = Integer.parseInt(args[0]);
            	height = Integer.parseInt(args[1]);
            	n = Integer.parseInt(args[2]);
            	k = Integer.parseInt(args[3]);
            	initialK = k;
            	assert k>=n && width>8 && height>8 && k>8 && n > 0;
            }
            //create grid
            grid = new Grid(width, height);
            //time
            long startTime = System.currentTimeMillis();
            //start n threads
            createStart();
            //wait till n threads finish
            aFaultGenerators.forEach(t -> {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
            //calculate run time
            long endTime = System.currentTimeMillis();
            //output images
            System.out.println(
            		"Fault Generation Complete with "+ initialK + " faults and " + n +" threads for "+width + " * "+height +".\n"
            		+ "Run Time: " + (endTime - startTime) + " ms \n" 
            		+ "Now writting to image");
            
            draw();
        } catch (Exception e) {
            System.out.println("ERROR " +e);
            e.printStackTrace();
        }
    }
    
    private static void printGrid() {
    	for(int i = 0 ; i < width * height; i++) {
        	System.out.print( grid.getNodes().get(i).getHeight()+" ");
        	if (i % width == width - 1 )System.out.println(" ");
        }
    }
    private static void draw() {
    	try {
    		int scale = 50;
            int min = grid.minHeight();
            int max = grid.maxHeight();
            // once we know what size we want we can create an empty image
            BufferedImage outputimage = new BufferedImage(width*scale,height*scale,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = outputimage.createGraphics();
            for(int i=0;i<width;i++) {
            	for(int j=0;j<height;j++) {
            		Node node = grid.find(i, j);
            		//Linearly map height to a color 
            		g2.setColor(new Color(0,0,(node.getHeight()-min)*(255-0)/(max-min) + 0 ));
            		g2.fillRect(i*scale, j*scale, scale, scale);
            	}
            }

            File outputfile = new File("outputimage.png");
            ImageIO.write(outputimage, "png", outputfile);
    	}catch (Exception e) {
            System.out.println("ERROR " +e);
            e.printStackTrace();
        }
        System.out.println("Image Done");
    }
    
    private static void createStart() {
    	for(int i= 0 ; i<n ; i++) {
        	aFaultGenerators.add(new FaultGenerator());
        }
    	aFaultGenerators.forEach(FaultGenerator::start);
    }
    
    
    
    static class FaultGenerator extends Thread{
		@Override
		public void run() {
			while(true) {
					if(k==0) break;
					else {
						k--;
						drawFault();
				}
			}
			
		}
		
		private void drawFault() {
			ArrayList<Node> Vector = grid.randomVector();
			Node p0 = Vector.get(0);
			Node p1 = Vector.get(1);
			List<Node> nodesToElevate = grid.getNodes().stream().filter(node -> node.isLeftOf(p0, p1)).collect(Collectors.toList());
			for(Node node : nodesToElevate) {
			
					node.incrementH(rand.nextInt(11));
				
					
				
			}
		}
	}
}
class Node {
	private int aX;
	private int aY;
	private int aH=0;
	
	public Node(int pX, int pY) {
		aX = pX ;
		aY = pY ; 
	}
	
	int getX() {
		return aX;
	}
	int getY() {
		return aY;
	}
	boolean isLeftOf (Node pNode0, Node pNode1) {
		return (pNode1.aX-pNode0.aX)*(this.aY-pNode0.aY)-(this.aX-pNode0.aX)*(pNode1.aY-pNode0.aY) > 0 ;
	}
	synchronized void incrementH(int h) {
		aH += h ; 
	}
	int getHeight() {
		return aH;
	}

}
class Grid{
	private static Random rand = new Random();
	private int aWidth; 
	private int aHeight;
	private final ArrayList<Node> aNodes= new ArrayList<>();
	private final ArrayList<Node> aEdgeNodes = new ArrayList<>();
	
	public Grid(int pWidth, int pHeight) {
		aWidth = pWidth ; 
		aHeight= pHeight;
		for(int i = 0; i < pWidth; i++) {
			for(int j = 0  ; j <pHeight ; j ++) {
				Node a  = new Node (i,j);
				aNodes.add(a);
				if(i == 0 || j==0 || i== aWidth-1 || j== aHeight - 1 ) {
					aEdgeNodes.add(a);
				}
			}
		}
	}
	
	private boolean isCorner(Node p0) {
		if(p0.getX() == 0) {
			if (p0.getY()==0)return true;
			else if (p0.getY() == aHeight -1)return true;
			else return false;
		}else if (p0.getX() == aWidth - 1) {
			if (p0.getY()==0)return true;
			else if (p0.getY() == aHeight -1)return true;
			else return false;
		}else return false;
	}
	
	
	private boolean onTheSameEdge(Node p0,Node p1) {
		// For edge points p0, p1 ; they are on the same edge iff they have at least one coordinate in commons
		
		if(p0.getX()==p1.getX()) {
			//case where two points are the same
			if(p0.getY()==p1.getY())return true;
			//if p0 is a corner, then any nodes with the same x is on the same edge
			if(isCorner(p0))return true;
			//if p0 is not a corner then the nodes on its edge cannot be height-apart.
			if(Math.abs(p0.getY()-p1.getY())<aHeight-1) return true;
			else return false;
		}
		else if(p0.getY() == p1.getY()){
			//if p0 is a corner, then any nodes with the same y is on the same edge
			if(isCorner(p0))return true;
			//if p0 is not a corner then the nodes on its edge cannot be width-apart.
			if(Math.abs(p0.getX()-p1.getX())<aWidth-1) return true;
			else return false;
		}else {
			return false;
		}
	}
	ArrayList<Node> randomVector(){
		ArrayList<Node> result = new ArrayList<>();
		Node p0 = aEdgeNodes.get(rand.nextInt(aEdgeNodes.size()));
		List<Node> l = aEdgeNodes.stream().filter(node -> !onTheSameEdge(p0, node)).collect(Collectors.toList()); 
		Node p1 = l.get(rand.nextInt(l.size()));	
		result.add(p0);	
		result.add(p1);
		return result;
	}
	ArrayList<Node> getNodes(){
		return aNodes;
	}
	
	int maxHeight() {
		return Collections.max(aNodes.stream().map(Node::getHeight).collect(Collectors.toList()));
	}
	int minHeight() {
		return Collections.min(aNodes.stream().map(Node::getHeight).collect(Collectors.toList()));
	}
	Node find(int x, int y) {
		return aNodes.get(x*aHeight+y);
	}
	
}

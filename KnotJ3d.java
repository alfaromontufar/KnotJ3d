import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame; 
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Cylinder;
import java.util.LinkedList;
import com.sun.j3d.utils.geometry.Text2D;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class KnotJ3d extends Applet implements ActionListener {

    private double radio = 0.01; // 0.01
    private double R= 0.1; 
    private double partition = 0.25; //0.25
    private double partition1 = 0.1; //0.1
    private double bezier = 0.25;   

    JTextField text = new JTextField(20);
    SimpleUniverse simpleU;
    Canvas3D canvas3D;

    private double[] getAngle(double[] vX, double[] vY){

        double[] angle = new double [2];
        angle[0] = 0.0; angle[1] = 0.0;

        double cos, sin;

        double dx = vY[0]-vX[0];
        double dy = vY[1]-vX[1];
        double dz = vY[2]-vX[2];

        double[] vec = new double [3];

        vec[0] = dx;
        vec[1] = dy;
        vec[2] = dz;

        if(dx*dx+dy*dy>0){
            cos = dy/Math.sqrt(dx*dx+dy*dy);
            sin = -dx/Math.sqrt(dx*dx+dy*dy);
	    vec[0] =cos*dx+sin*dy;
	    vec[1] =-sin*dx+cos*dy;
            if( dx > 0 )
                angle[0] = -Math.acos(cos);
            else
                angle[0] = Math.acos(cos);
        }

        if(vec[1]*vec[1]+vec[2]*vec[2]>0){          
           cos = vec[1]/Math.sqrt(vec[1]*vec[1]+vec[2]*vec[2]);            	   
	   if( vec[2]>0 )
	       angle[1] = Math.acos(cos);		 		 
	   else
	       angle[1] = -Math.acos(cos);
        } 
   
        return angle;
    }

    private TransformGroup createText(String cadena, Nodo position){

	float lenght = (float) 0.1;

	TransformGroup objTransform = new TransformGroup();

	Transform3D translate = new Transform3D();	    
	translate.set(new Vector3f((float) (position.x>0 ? position.x + lenght - 0.08 : position.x - lenght) , (float) (position.y>0 ? position.y + lenght - 0.06 : position.y - lenght), (float) 0.0 ));
	TransformGroup objTranslate = new TransformGroup(translate);
	objTransform.addChild(objTranslate);	    
	
	TransformGroup objSpin = new TransformGroup();
	objSpin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	objTranslate.addChild(objSpin);
	
	Text2D text2d = new Text2D(cadena , new Color3f(0.0f, 0.0f, 0.0f), "Helvetica", 12, 0);
	
	objSpin.addChild(text2d);
	
	Appearance textAppear = text2d.getAppearance();
	
	PolygonAttributes polyAttrib = new PolygonAttributes();
	polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
	polyAttrib.setBackFaceNormalFlip(true);
	textAppear.setPolygonAttributes(polyAttrib);

	return objTransform;
	
    }

    private TransformGroup createLine(double[] vX, double[] vY) {

	TransformGroup objTransform = new TransformGroup();               		
	    
	Transform3D translate = new Transform3D();	    
	translate.set(new Vector3f((float) ((vX[0]+vY[0])/2.0), (float) ((vX[1]+vY[1])/2.0), (float) ((vX[2]+vY[2])/2.0)));
	TransformGroup objTranslate = new TransformGroup(translate);
	objTransform.addChild(objTranslate);
	Transform3D rotate = new Transform3D();
	Transform3D tempRotate = new Transform3D();	    
	double [] angle = new double [2];
	angle = getAngle(vX,vY);	    
	rotate.rotZ(angle[0]);	    
	tempRotate.rotX(angle[1]);
	rotate.mul(tempRotate);	    
	TransformGroup objRotate = new TransformGroup(rotate);
	objTranslate.addChild(objRotate);	    
	objRotate.addChild(new Cylinder((float)radio,(float) Math.sqrt((vX[0]-vY[0])*(vX[0]-vY[0]) +(vX[1]-vY[1])*(vX[1]-vY[1]) +(vX[2]-vY[2])*(vX[2]-vY[2]) )));

	Transform3D translate1 = new Transform3D();	    
	translate1.set(new Vector3f((float) vY[0], (float) vY[1], (float) vY[2]));
	TransformGroup objTranslate1 = new TransformGroup(translate1);
	objTransform.addChild(objTranslate1);
	objTranslate1.addChild(new Sphere((float)radio));

	Transform3D translate2 = new Transform3D();	    
	translate1.set(new Vector3f((float) vX[0], (float) vX[1], (float) vX[2]));
	TransformGroup objTranslate2 = new TransformGroup(translate1);
	objTransform.addChild(objTranslate2);
	objTranslate2.addChild(new Sphere((float)radio));

	return objTransform;

    }

    private LinkNodo createTangle (String cadena){

	//// Calculating number of Nodes
	
	int nNumbers = 1;
	
	for(int i=0; i<cadena.length(); ++i)
	    if(cadena.charAt(i)== ',')
		nNumbers++;
	
	int[ ] number = new int [nNumbers];	
	
	int index=0;
	
	for( int i=0; cadena.indexOf(',',index) != -1; ++i){	    
	    number[i] = Integer.parseInt(cadena.substring(index,cadena.indexOf(',',index)));	    	    	   
	    index = cadena.indexOf(',',index) + 1;	    
	}
	
	number[nNumbers-1] = Integer.parseInt(cadena.substring(index,cadena.length()));	
	
	//// Filling Nodes
	
	int sum = 0;
	
	Nodo nE = new Nodo();
	Nodo nW = new Nodo();
	Nodo sE = new Nodo();
	Nodo sW = new Nodo();
	
	LinkedList listA = new LinkedList();
	LinkedList listB = new LinkedList();
	LinkedList listC = new LinkedList();
	
	boolean flagC = false;

	for(int iNumber = 0 ; iNumber < nNumbers ; ++iNumber){


	    Nodo nEaux = new Nodo();
	    Nodo nWaux = new Nodo();
	    Nodo sEaux = new Nodo();
	    Nodo sWaux = new Nodo();

	    LinkedList listAaux = new LinkedList();
	    LinkedList listBaux = new LinkedList();

	    if(number[iNumber]>0){		
 	    		
		for (int i=0; i<2*number[iNumber]+1; ++i) {
		    Nodo Aux = new Nodo();
		    Aux.x = (double)( R * ( i - number[iNumber] ) );  
		    Aux.y = (double)( R * Math.cos(Math.PI*i/2.0) );
		    Aux.z = (double)( R * Math.sin(Math.PI*i/2.0) );
		    listAaux.addLast(Aux);
		    if( i == 0 )
			nWaux.equalize(Aux);		       
		    if( i == 2*number[iNumber] && number[iNumber]%2!=0)
			sEaux.equalize(Aux);
		    if( i == 2*number[iNumber] && number[iNumber]%2==0)
			nEaux.equalize(Aux);
		}
		
		for (int i=0; i<2*number[iNumber]+1; ++i) {
		    Nodo Aux = new Nodo();
		    Aux.x = (double)( R * ( i - number[iNumber] ) );
		    Aux.y = (double)( - R * Math.cos(Math.PI*i/2.0) );
		    Aux.z = (double)( - R * Math.sin(Math.PI*i/2.0) );
		    listBaux.addLast(Aux);
		    if( i == 0 )
			sWaux.equalize(Aux);
		    if( i == 2*number[iNumber] && number[iNumber]%2!=0)
			nEaux.equalize(Aux);
		    if( i == 2*number[iNumber] && number[iNumber]%2==0)
			sEaux.equalize(Aux);
		}
	    }	    
	    else if(number[iNumber]<0){

		for (int i=0; i<2*Math.abs(number[iNumber])+1; ++i) {
		    Nodo Aux = new Nodo();
		    Aux.x = (double)( R * ( i + number[iNumber] ) );
		    Aux.y = (double)(   R * Math.cos(Math.PI*i/2.0) );
		    Aux.z = (double)( - R * Math.sin(Math.PI*i/2.0) );		 
		    listAaux.addLast(Aux);
		    if( i == 0 )
			nWaux.equalize(Aux);
		    if( i == -2*number[iNumber] && number[iNumber]%2!=0)
			sEaux.equalize(Aux);
		    if( i == -2*number[iNumber] && number[iNumber]%2==0)
			nEaux.equalize(Aux);
		}
		
		for (int i=0; i<2*Math.abs(number[iNumber])+1; ++i) {
		    Nodo Aux = new Nodo();
		    Aux.x = (double)( R * ( i + number[iNumber] ) );
		    Aux.y = (double)( - R * Math.cos(Math.PI*i/2.0) );
		    Aux.z = (double)(   R * Math.sin(Math.PI*i/2.0) );		    
		    listBaux.addLast(Aux);
		    if( i == 0 )
			sWaux.equalize(Aux);
		    if( i == -2*number[iNumber] && number[iNumber]%2!=0)
			nEaux.equalize(Aux);
		    if( i == -2*number[iNumber] && number[iNumber]%2==0)
			sEaux.equalize(Aux);
		}
	    }
	    else{ //// i.e. number[iNumber] == 0

		for (int i=0; i< 3 ; ++i){
		    Nodo Aux = new Nodo();
		    Aux.x = (double)( - R * Math.cos(Math.PI*i/2.0)  );
		    Aux.y = (double)(   R * (0.6+0.4*Math.pow(-1.0,i)) );		    
		    listAaux.addLast(Aux);
		    if( i == 0 )
			nWaux.equalize(Aux);
		    if( i == 2)
			nEaux.equalize(Aux);
		}
		
		for (int i=0; i < 3 ; ++i){
		    Nodo Aux = new Nodo();
		    Aux.x = (double)( - R * Math.cos(Math.PI*i/2.0)  );
		    Aux.y = (double)( - R * (0.6+0.4*Math.pow(-1,i)) );
		    Aux.z = 0.0;		    
		    listBaux.addLast(Aux);
		    if( i == 0 )
			sWaux.equalize(Aux);
		    if( i == 2)
			sEaux.equalize(Aux);
		}		
	    }	   	   

	    if(iNumber == 0){
		
		nW.equalize(nWaux);
		nE.equalize(nEaux);
		sW.equalize(sWaux);
		sE.equalize(sEaux);
		
		while(listAaux.size() > 0){
		    listA.addLast( (Nodo) listAaux.removeFirst() );
		    listB.addLast( (Nodo) listBaux.removeFirst() );
		}

	    }else{  //// More than 1 numbers	       		
		
		//// Rotate 90 grades
		
		for(int i=0; i<listA.size() ; ++i){
		    
		    Nodo aux = new Nodo();		    		   
		    
		    aux = (Nodo) listA.removeFirst();
		    aux.rotate90();
		    listA.addLast(aux);
		    
		}

		for(int i=0; i<listB.size() ; ++i){
		    
		    Nodo aux = new Nodo();

		    aux = (Nodo) listB.removeFirst();
		    aux.rotate90();
		    listB.addLast(aux);
		    
		}
		
		Nodo auxiliar = new Nodo();

		nW.rotate90();
		nE.rotate90();
		sE.rotate90();
		sW.rotate90();

		auxiliar.equalize(nW);
		nW.equalize(nE);
		nE.equalize(sE);
		sE.equalize(sW);
		sW.equalize(auxiliar);	

		//// Rotate horizontal axis & - operation				
		
		for(int i=0; i<listA.size() ; ++i){
		    
		    Nodo aux = new Nodo();
		    
		    aux = (Nodo) listA.removeFirst();
		    aux.rotateh();
		    listA.addLast(aux);

		}

		for(int i=0; i<listB.size() ; ++i){
		    
		    Nodo aux = new Nodo();
    
		    aux = (Nodo) listB.removeFirst();
		    aux.rotateh();
		    listB.addLast(aux);
		    
		}

		nW.rotateh();
		nE.rotateh();
		sE.rotateh();
		sW.rotateh();

		auxiliar.equalize(nW);
		nW.equalize(sW);
		sW.equalize(auxiliar);
		auxiliar.equalize(nE);
		nE.equalize(sE);
		sE.equalize(auxiliar);
		
		//// Move to origin

		double wide1 = (nE.x>sE.x)? (double) ( nE.x + R ): (double) ( sE.x + R );
		double wide2= (nEaux.x > sEaux.x)? (double) ( nEaux.x + R ): (double) ( sEaux.x + R );		
		
		for(int i=0; i<listAaux.size() ; ++i){
		    
		    Nodo aux = new Nodo();
		    
		    aux = (Nodo) listAaux.removeFirst();
		    aux.moveX(wide1);
		    listAaux.addLast(aux);

		}
		    
		for(int i=0; i<listBaux.size() ; ++i){
		    
		    Nodo aux = new Nodo();

		    aux = (Nodo) listBaux.removeFirst();
		    aux.moveX(wide1);
		    listBaux.addLast(aux);
		    
		}
		    
		for(int i=0; i<listA.size() ; ++i){
		    
		    Nodo aux = new Nodo();

		    aux = (Nodo) listA.removeFirst();
		    aux.moveX(-wide2);
		    listA.addLast(aux);

		}
		
		for(int i=0; i<listB.size() ; ++i){
		    
		    Nodo aux = new Nodo();
		    
		    aux = (Nodo) listB.removeFirst();
		    aux.moveX(-wide2);
		    listB.addLast(aux);
		    
		}

		nW.moveX(-wide2);
		nE.moveX(-wide2);
		sE.moveX(-wide2);
		sW.moveX(-wide2);
		nWaux.moveX(wide1);
		nEaux.moveX(wide1);
		sEaux.moveX(wide1);
		sWaux.moveX(wide1);

		if(nE.equals( (Nodo) listA.getFirst()))
		    for(int i=1; i < listA.size(); i++)		
			listA.addFirst( (Nodo) listA.remove(i) );
		
		if(nE.equals( (Nodo) listB.getFirst()))
		    for(int i=1; i < listB.size(); i++)		
			listB.addFirst( (Nodo) listB.remove(i) );
		
		if(nWaux.equals( (Nodo) listAaux.getLast()))
		    for(int i=1; i < listAaux.size(); i++)		
			listAaux.addFirst( (Nodo) listAaux.remove(i) );
		
		if(nWaux.equals( (Nodo) listBaux.getLast()))
		    for(int i=1; i < listBaux.size(); i++)		
			listBaux.addFirst( (Nodo) listBaux.remove(i) );

		if(nE.equals( (Nodo) listA.getLast())){ 	
		    if(nWaux.equals( (Nodo) listAaux.getFirst() )){ 
			while(listAaux.size() > 0)
			    listA.addLast( (Nodo) listAaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listA.getLast() )){
			    if(sE.equals( (Nodo) listB.getFirst() )){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeFirst() );
				
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getLast() ) ){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeLast() );
				
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getFirst() ) ){
				System.out.println( "Error SE(A) #1" );
				while(listA.size() > 0)
				    listC.addLast( (Nodo) listA.removeFirst() );
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );
			    }				
			}
			
			if(sE.equals( (Nodo) listA.getFirst() )){ ///

			    if(sWaux.equals( (Nodo) listBaux.getLast() ))
				for(int i=1; i < listBaux.size(); i++)		
				    listBaux.addFirst( (Nodo) listBaux.remove(i) );

			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))			
				while(listBaux.size() > 0)
				    listA.addFirst( (Nodo) listBaux.removeFirst() );
			    
			}
			
			if(listB.size() > 0 && sE.equals( (Nodo) listB.getFirst()))
			    for(int i=1; i < listB.size(); i++)		
				listB.addFirst( (Nodo) listB.remove(i) );
			
			if(listBaux.size() > 0 && sWaux.equals( (Nodo) listBaux.getLast()))
			    for(int i=1; i < listBaux.size(); i++)		
				listBaux.addFirst( (Nodo) listBaux.remove(i) ); 		
			
			if(listB.size() > 0 && sE.equals( (Nodo) listB.getLast()))
			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );		       
			
		    }else if(nWaux.equals( (Nodo) listBaux.getFirst() )){ //		

			while(listBaux.size() > 0)
			    listA.addLast( (Nodo) listBaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listA.getLast() )){
			    if(sE.equals( (Nodo) listB.getFirst() )){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeFirst() );
				
				while(listAaux.size() > 0)
				    listB.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getLast() ) ){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeLast() );
				
				while(listAaux.size() > 0)
				    listB.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getFirst() ) )
				System.out.println( "Error SE(A) #2" );
			}
			
			if(sE.equals( (Nodo) listA.getFirst() )){ /////Corregir			   
			    if(sWaux.equals( (Nodo) listAaux.getLast() ))
				for(int i=1; i < listAaux.size(); i++)		
				    listAaux.addFirst( (Nodo) listAaux.remove(i) );

			    if(sWaux.equals( (Nodo) listAaux.getFirst() ))			
				while(listAaux.size() > 0)
				    listA.addFirst( (Nodo) listAaux.removeFirst() );
			    
			}

			if(listB.size() > 0 && sE.equals( (Nodo) listB.getFirst()))
			    for(int i=1; i < listB.size(); i++)		
				listB.addFirst( (Nodo) listB.remove(i) );
			
			if(listAaux.size() > 0 && sW.equals( (Nodo) listAaux.getLast()))
			    for(int i=1; i < listAaux.size(); i++)		
				listAaux.addFirst( (Nodo) listAaux.remove(i) );
			
			if(sE.equals( (Nodo) listB.getLast()))
			    if(sW.equals( (Nodo) listAaux.getFirst() ))
				while(listAaux.size() > 0)
				    listB.addLast( (Nodo) listAaux.removeFirst() );								
			
		    }
		    
		} else if(nE.equals( (Nodo) listB.getLast())){ ///		

		    if(nWaux.equals( (Nodo) listAaux.getFirst() )){ //
			
			while(listAaux.size() > 0)
			    listB.addLast( (Nodo) listAaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listB.getLast() )){
			    if(sE.equals( (Nodo) listA.getFirst() )){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeFirst() );
				
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getLast() ) ){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeLast() );
				
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getFirst() ) )
				{
				    System.out.println( "Error SE(B) #1" );
				    while(listB.size() > 0)
					listC.addLast( (Nodo) listB.removeFirst() );
				    while(listBaux.size() > 0)
					listB.addLast( (Nodo) listBaux.removeFirst() );
				}
			}		
			
			if(sE.equals( (Nodo) listB.getFirst() )){		       

			    if(sWaux.equals( (Nodo) listBaux.getLast() ))
				for(int i=1; i < listBaux.size(); i++)		
				    listBaux.addFirst( (Nodo) listBaux.remove(i) );

			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))	       
				while(listBaux.size() > 0)
				    listB.addFirst( (Nodo) listBaux.removeFirst() );
			    
			}

			if(listA.size() > 0 && sE.equals( (Nodo) listA.getFirst()))
			    for(int i=1; i < listA.size(); i++)		
				listA.addFirst( (Nodo) listA.remove(i) );
			
			if(listBaux.size() > 0 && sWaux.equals( (Nodo) listBaux.getLast()))
			    for(int i=1; i < listBaux.size(); i++)		
				listBaux.addFirst( (Nodo) listBaux.remove(i) );
			
			if(sE.equals( (Nodo) listA.getLast()))
			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );								
			
		    }else if(nWaux.equals( (Nodo) listBaux.getFirst() )){ //		       		
			
			while(listBaux.size() > 0)
			    listB.addLast( (Nodo) listBaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listB.getLast() )){
			    if(sE.equals( (Nodo) listA.getFirst() )){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeFirst() );
				
				while(listAaux.size() > 0)
				    listA.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getLast() ) ){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeLast() );
				
				while(listAaux.size() > 0)
				    listA.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getFirst() ) )
				System.out.println( "Error SE(B) #2" );
			}
			
			if(sE.equals( (Nodo) listB.getFirst() )){

			    if(sWaux.equals( (Nodo) listAaux.getLast() ))
				for(int i=1; i < listAaux.size(); i++)		
				    listBaux.addFirst( (Nodo) listAaux.remove(i) );

			    if(sWaux.equals( (Nodo) listAaux.getFirst() ))
				while(listAaux.size() > 0)
				    listB.addFirst( (Nodo) listAaux.removeFirst() );
			    
			}

			if(listA.size() > 0 && sE.equals( (Nodo) listA.getFirst()))
			    for(int i=1; i < listA.size(); i++)		
				listA.addFirst( (Nodo) listA.remove(i) );
			
			if(listAaux.size() > 0 && sW.equals( (Nodo) listAaux.getLast()))
			    for(int i=1; i < listAaux.size(); i++)		
				listAaux.addFirst( (Nodo) listAaux.remove(i) );
			
			if(sE.equals( (Nodo) listA.getLast()))
			    if(sW.equals( (Nodo) listAaux.getFirst() ))
				while(listAaux.size() > 0)
				    listA.addLast( (Nodo) listAaux.removeFirst() );								
		    }
		}else 
		    System.out.print(" Error #3 Puntos Cardinales");		    							

		nE.equalize(nEaux);
		sE.equalize(sEaux);

	    }
	}	

	LinkNodo llReturned = new LinkNodo ();

	llReturned.equalize(nE, nW, sE, sW, listA, listB);

	return llReturned;

    }

    private TransformGroup createKnot (String cadena){

	int numberPlus = 0;

	boolean isthereN = false;

	if( cadena.charAt(0)=='N' ){
	    isthereN = true;
	    cadena = cadena.substring(2,cadena.length()-1);
	}	    

	for(int i=0; i<cadena.length(); ++i)
	    if(cadena.charAt(i)== '+')
		numberPlus++;

	Nodo nE = new Nodo();
	Nodo nW = new Nodo();
	Nodo sE = new Nodo();
	Nodo sW = new Nodo();
	
	LinkedList listA = new LinkedList();
	LinkedList listB = new LinkedList();
	LinkedList[] listC = new LinkedList[numberPlus];

	for(int i=0; i < numberPlus; i++)
	    listC[i] = new LinkedList();

	int indexPlus = 0;

	int iAux = numberPlus;
	int jAux = 0;	

	while(iAux>-1){

	    String auxString;

	    if(iAux>0)
		auxString = cadena.substring(jAux,cadena.indexOf('+',jAux));
	    else
		auxString = cadena.substring(jAux);

	    Nodo nEaux = new Nodo();
	    Nodo nWaux = new Nodo();
	    Nodo sEaux = new Nodo();
	    Nodo sWaux = new Nodo();
	    
	    LinkedList listAaux = new LinkedList();
	    LinkedList listBaux = new LinkedList();
	    
	    LinkNodo linkaux = createTangle (auxString);
	    
	    nEaux = linkaux.nE;
	    nWaux = linkaux.nW;
	    sEaux = linkaux.sE;
	    sWaux = linkaux.sW;
	    
	    listAaux = linkaux.listA;
	    listBaux = linkaux.listB;	    

	    if(iAux == numberPlus){

		nE = nEaux;
		nW = nWaux;
		sE = sEaux;
		sW = sWaux;
		
		listA = listAaux;
		listB = listBaux;
		
	    }else{

		double wide1 = (nE.x>sE.x)? (double) ( nE.x + R ): (double) ( sE.x + R );
		double wide2= (nEaux.x > sEaux.x)? (double) ( nEaux.x + R ): (double) ( sEaux.x + R );		
		
		for(int i=0; i<listAaux.size() ; ++i){
		    
		    Nodo aux = new Nodo();
		    
		    aux = (Nodo) listAaux.removeFirst();
		    aux.moveX(wide1);
		    listAaux.addLast(aux);

		}
		    
		for(int i=0; i<listBaux.size() ; ++i){
		    
		    Nodo aux = new Nodo();

		    aux = (Nodo) listBaux.removeFirst();
		    aux.moveX(wide1);
		    listBaux.addLast(aux);
		    
		}
		    
		for(int i=0; i<listA.size() ; ++i){
		    
		    Nodo aux = new Nodo();

		    aux = (Nodo) listA.removeFirst();
		    aux.moveX(-wide2);
		    listA.addLast(aux);

		}
		
		for(int i=0; i<listB.size() ; ++i){
		    
		    Nodo aux = new Nodo();
		    
		    aux = (Nodo) listB.removeFirst();
		    aux.moveX(-wide2);
		    listB.addLast(aux);
		    
		}

		nW.moveX(-wide2);
		nE.moveX(-wide2);
		sE.moveX(-wide2);
		sW.moveX(-wide2);
		nWaux.moveX(wide1);
		nEaux.moveX(wide1);
		sEaux.moveX(wide1);
		sWaux.moveX(wide1);

		for(int index=0; index<numberPlus; index++ ){
		    for(int i=0; i<listC[index].size(); ++i){			
			Nodo aux = new Nodo();
			aux = (Nodo) listC[index].removeFirst();
			aux.moveX(-wide2);
			listC[index].addLast(aux);
		    }
		}

		if(nE.equals( (Nodo) listA.getFirst()))
		    for(int i=1; i < listA.size(); i++)		
			listA.addFirst( (Nodo) listA.remove(i) );
		
		if(nE.equals( (Nodo) listB.getFirst()))
		    for(int i=1; i < listB.size(); i++)		
			listB.addFirst( (Nodo) listB.remove(i) );
		
		if(nWaux.equals( (Nodo) listAaux.getLast()))
		    for(int i=1; i < listAaux.size(); i++)		
			listAaux.addFirst( (Nodo) listAaux.remove(i) );
		
		if(nWaux.equals( (Nodo) listBaux.getLast()))
		    for(int i=1; i < listBaux.size(); i++)		
			listBaux.addFirst( (Nodo) listBaux.remove(i) );

		if(nE.equals( (Nodo) listA.getLast())){ 	
		    if(nWaux.equals( (Nodo) listAaux.getFirst() )){ 
			while(listAaux.size() > 0)
			    listA.addLast( (Nodo) listAaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listA.getLast() )){
			    if(sE.equals( (Nodo) listB.getFirst() )){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeFirst() );
				
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getLast() ) ){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeLast() );
				
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getFirst() ) ){
				System.out.println( "Error SE(A) #1" );

				while(listA.size() > 0)
				    listC[indexPlus].addLast( (Nodo) listA.removeFirst() );
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );

				indexPlus++;
			    }
			}
			
			if(sE.equals( (Nodo) listA.getFirst() )){ ///

			    if(sWaux.equals( (Nodo) listBaux.getLast() ))
				for(int i=1; i < listBaux.size(); i++)		
				    listBaux.addFirst( (Nodo) listBaux.remove(i) );

			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))			
				while(listBaux.size() > 0)
				    listA.addFirst( (Nodo) listBaux.removeFirst() );
			    
			}
			
			if(listB.size() > 0 && sE.equals( (Nodo) listB.getFirst()))
			    for(int i=1; i < listB.size(); i++)		
				listB.addFirst( (Nodo) listB.remove(i) );
			
			if(listBaux.size() > 0 && sWaux.equals( (Nodo) listBaux.getLast()))
			    for(int i=1; i < listBaux.size(); i++)		
				listBaux.addFirst( (Nodo) listBaux.remove(i) ); 		
			
			if(listB.size() > 0 && sE.equals( (Nodo) listB.getLast()))
			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );		       
			
		    }else if(nWaux.equals( (Nodo) listBaux.getFirst() )){ //		

			while(listBaux.size() > 0)
			    listA.addLast( (Nodo) listBaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listA.getLast() )){
			    if(sE.equals( (Nodo) listB.getFirst() )){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeFirst() );
				
				while(listAaux.size() > 0)
				    listB.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getLast() ) ){
				
				while(listB.size() > 0)
				    listA.addLast( (Nodo) listB.removeLast() );
				
				while(listAaux.size() > 0)
				    listB.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getFirst() ) )
				System.out.println( "Error SE(A) #2" );
			}
			
			if(sE.equals( (Nodo) listA.getFirst() )){ /////Corregir			   
			    if(sWaux.equals( (Nodo) listAaux.getLast() ))
				for(int i=1; i < listAaux.size(); i++)		
				    listAaux.addFirst( (Nodo) listAaux.remove(i) );

			    if(sWaux.equals( (Nodo) listAaux.getFirst() ))			
				while(listAaux.size() > 0)
				    listA.addFirst( (Nodo) listAaux.removeFirst() );
			    
			}

			if(listB.size() > 0 && sE.equals( (Nodo) listB.getFirst()))
			    for(int i=1; i < listB.size(); i++)		
				listB.addFirst( (Nodo) listB.remove(i) );
			
			if(listAaux.size() > 0 && sW.equals( (Nodo) listAaux.getLast()))
			    for(int i=1; i < listAaux.size(); i++)		
				listAaux.addFirst( (Nodo) listAaux.remove(i) );
			
			if(sE.equals( (Nodo) listB.getLast()))
			    if(sW.equals( (Nodo) listAaux.getFirst() ))
				while(listAaux.size() > 0)
				    listB.addLast( (Nodo) listAaux.removeFirst() );								
			
		    }
		    
		} else if(nE.equals( (Nodo) listB.getLast())){ ///		

		    if(nWaux.equals( (Nodo) listAaux.getFirst() )){ //
			
			while(listAaux.size() > 0)
			    listB.addLast( (Nodo) listAaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listB.getLast() )){
			    if(sE.equals( (Nodo) listA.getFirst() )){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeFirst() );
				
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getLast() ) ){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeLast() );
				
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getFirst() ) ){
				System.out.println( "Error SE(B) #1" );
				
				while(listB.size() > 0)
				    listC[indexPlus].addLast( (Nodo) listB.removeFirst() );
				while(listBaux.size() > 0)
				    listB.addLast( (Nodo) listBaux.removeFirst() );

				indexPlus++;
			    }
			}		

			if(sE.equals( (Nodo) listB.getFirst() )){		       

			    if(sWaux.equals( (Nodo) listBaux.getLast() ))
				for(int i=1; i < listBaux.size(); i++)		
				    listBaux.addFirst( (Nodo) listBaux.remove(i) );

			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))	       
				while(listBaux.size() > 0)
				    listB.addFirst( (Nodo) listBaux.removeFirst() );
			    
			}

			if(listA.size() > 0 && sE.equals( (Nodo) listA.getFirst()))
			    for(int i=1; i < listA.size(); i++)		
				listA.addFirst( (Nodo) listA.remove(i) );
			
			if(listBaux.size() > 0 && sWaux.equals( (Nodo) listBaux.getLast()))
			    for(int i=1; i < listBaux.size(); i++)		
				listBaux.addFirst( (Nodo) listBaux.remove(i) );
			
			if(sE.equals( (Nodo) listA.getLast()))
			    if(sWaux.equals( (Nodo) listBaux.getFirst() ))
				while(listBaux.size() > 0)
				    listA.addLast( (Nodo) listBaux.removeFirst() );								
			
		    }else if(nWaux.equals( (Nodo) listBaux.getFirst() )){ //		       		
			
			while(listBaux.size() > 0)
			    listB.addLast( (Nodo) listBaux.removeFirst() );
			
			if(sWaux.equals( (Nodo) listB.getLast() )){
			    if(sE.equals( (Nodo) listA.getFirst() )){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeFirst() );
				
				while(listAaux.size() > 0)
				    listA.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listA.getLast() ) ){
				
				while(listA.size() > 0)
				    listB.addLast( (Nodo) listA.removeLast() );
				
				while(listAaux.size() > 0)
				    listA.addLast( (Nodo) listAaux.removeFirst() );
				
			    }else if( sE.equals( (Nodo) listB.getFirst() ) )
				System.out.println( "Error SE(B) #2" );
			}
			
			if(sE.equals( (Nodo) listB.getFirst() )){

			    if(sWaux.equals( (Nodo) listAaux.getLast() ))
				for(int i=1; i < listAaux.size(); i++)		
				    listBaux.addFirst( (Nodo) listAaux.remove(i) );

			    if(sWaux.equals( (Nodo) listAaux.getFirst() ))
				while(listAaux.size() > 0)
				    listB.addFirst( (Nodo) listAaux.removeFirst() );
			    
			}

			if(listA.size() > 0 && sE.equals( (Nodo) listA.getFirst()))
			    for(int i=1; i < listA.size(); i++)		
				listA.addFirst( (Nodo) listA.remove(i) );
			
			if(listAaux.size() > 0 && sW.equals( (Nodo) listAaux.getLast()))
			    for(int i=1; i < listAaux.size(); i++)		
				listAaux.addFirst( (Nodo) listAaux.remove(i) );
			
			if(sE.equals( (Nodo) listA.getLast()))
			    if(sW.equals( (Nodo) listAaux.getFirst() ))
				while(listAaux.size() > 0)
				    listA.addLast( (Nodo) listAaux.removeFirst() );								
		    }
		}else 
		    System.out.print(" Error #3 Puntos Cardinales");		    							

		nE.equalize(nEaux);
		sE.equalize(sEaux);

	    }

	    jAux = cadena.indexOf('+',jAux)+1;

	    iAux--;
	}

	TransformGroup objTransform = new TransformGroup();

	if(!isthereN){

	    objTransform.addChild(createText("NE",nE));
	    objTransform.addChild(createText("NW",nW));
	    objTransform.addChild(createText("SE",sE));
	    objTransform.addChild(createText("SW",sW));	    	    

	}

	double[ ][ ] b = new double[4][3];

	Nodo node[] = new Nodo[4];
	for(int i=0; i<4; ++i)
	    node[i] = new Nodo();

	double[] vX = new double[3];
	double[] vY = new double[3];   

 	Nodo Aux = new Nodo();

	double min = 0.0;	
	double max = 0.0;

	int N;
	for(int index=0; index<numberPlus; index++ ){
	    if(listC[index].size()>0){
		
		Nodo last = (Nodo) listC[index].getLast();
		Nodo first = (Nodo) listC[index].get(0);
		Nodo firstPlus = (Nodo) listC[index].get(1);	   
		
		listC[index].addFirst(last);
		listC[index].addLast(first);
		listC[index].addLast(firstPlus);
		
		N = listC[index].size();
		
		for (int i=1; i<(N-2); ++i) {
		    if(i != 0)
			node[0] = (Nodo) listC[index].get(i-1);
		    node[1] = (Nodo) listC[index].get(i);
		    
		    if(min>node[1].y)
			min = node[1].y;
		    
		    if(max<node[1].y)
			max = node[1].y;
		    
		    node[2] = (Nodo) listC[index].get(i+1);
		    if(i != N-2)
			node[3] = (Nodo) listC[index].get(i+2);
		    
		    b[0][0] = node[1].x;
		    b[1][0] = node[1].x + (node[2].x - node[ (i == 0 ? 1 : 0) ].x)*bezier;
		    b[2][0] = node[2].x - (node[ (i == N-2 ? 2 : 3) ].x - node[1].x)*bezier;
		    b[3][0] = node[2].x;
		    
		    b[0][1] = node[1].y;
		    b[1][1] = node[1].y + (node[2].y - node[ (i == 0 ? 1 : 0) ].y)*bezier;
		    b[2][1] = node[2].y - (node[ (i == N-2 ? 2 : 3) ].y - node[1].y)*bezier;
		    b[3][1] = node[2].y;
		    
		    b[0][2] = node[1].z;
		    b[1][2] = node[1].z + (node[2].z - node[ (i == 0 ? 1 : 0) ].z)*bezier;
		    b[2][2] = node[2].z - (node[ (i == N-2 ? 2 : 3) ].z - node[1].z)*bezier;
		    b[3][2] = node[2].z;
		    
		    for(double j=0.0; j <= 1.0; j += partition){	      
			vY[0] = 0; 
			for(int k=0; k<4; ++k)
			    vY[0] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][0] * (k == 0 || k == 3 ? 1.0 : 3.0);
			
			vY[1] = 0; 
			for(int k=0; k<4; ++k)
			    vY[1] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][1]* (k == 0 || k == 3 ? 1.0 : 3.0);
			
			vY[2] = 0; 
			for(int k=0; k<4; ++k)
			    vY[2] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][2]* (k == 0 || k == 3 ? 1.0 : 3.0);
			
			if( j > 0.0 )
			    objTransform.addChild(createLine(vX,vY));
			
			vX[0] = vY[0];
			vX[1] = vY[1];
			vX[2] = vY[2];		
			
		    }
		    
		}
	    }
	}

	N = listA.size();

	for (int i=0; i<(N-1); ++i) {	
	    if(i != 0)
		node[0] = (Nodo) listA.get(i-1);
	    node[1] = (Nodo) listA.get(i);

	    if(max<node[1].y)
		max = node[1].y;
		
	    node[2] = (Nodo) listA.get(i+1);
	    if(i != N-2)
		node[3] = (Nodo) listA.get(i+2);

	    b[0][0] = node[1].x;
	    b[1][0] = node[1].x + (node[2].x - node[ (i == 0 ? 1 : 0) ].x)*bezier;
	    b[2][0] = node[2].x - (node[ (i == N-2 ? 2 : 3) ].x - node[1].x)*bezier;
	    b[3][0] = node[2].x;

	    b[0][1] = node[1].y;
	    b[1][1] = node[1].y + (node[2].y - node[ (i == 0 ? 1 : 0) ].y)*bezier;
	    b[2][1] = node[2].y - (node[ (i == N-2 ? 2 : 3) ].y - node[1].y)*bezier;
	    b[3][1] = node[2].y;

	    b[0][2] = node[1].z;
	    b[1][2] = node[1].z + (node[2].z - node[ (i == 0 ? 1 : 0) ].z)*bezier;
	    b[2][2] = node[2].z - (node[ (i == N-2 ? 2 : 3) ].z - node[1].z)*bezier;
	    b[3][2] = node[2].z;
	    
	    for(double j=0.0; j <= 1.0; j += partition){	      
		vY[0] = 0; 
		for(int k=0; k<4; ++k)
		    vY[0] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][0] * (k == 0 || k == 3 ? 1.0 : 3.0);
		
		vY[1] = 0; 
		for(int k=0; k<4; ++k)
		    vY[1] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][1]* (k == 0 || k == 3 ? 1.0 : 3.0);
		
		vY[2] = 0; 
		for(int k=0; k<4; ++k)
		    vY[2] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][2]* (k == 0 || k == 3 ? 1.0 : 3.0);

		if( j > 0.0 )
		    objTransform.addChild(createLine(vX,vY));
		
		vX[0] = vY[0];
		vX[1] = vY[1];
		vX[2] = vY[2];		
	    
	    }
	    
	}

	if(isthereN){

	    b[0][0] = nW.x;
	    b[1][0] = nW.x;
	    b[2][0] = nE.x;
	    b[3][0] = nE.x;

	    b[0][1] = nW.y;
	    b[1][1] = nW.y+max*(nE.x-nW.x<1?1:nE.x-nW.x);
	    b[2][1] = nE.y+max*(nE.x-nW.x<1?1:nE.x-nW.x);
	    b[3][1] = nE.y;

	    b[0][2] = nW.z;
	    b[1][2] = nW.z;
	    b[2][2] = nE.z;
	    b[3][2] = nE.z;
	    
	    for(double j=0.0; j <= 1.0; j += partition1){     
		vY[0] = 0; 
		for(int k=0; k<4; ++k)
		    vY[0] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][0] * (k == 0 || k == 3 ? 1.0 : 3.0);
		
		vY[1] = 0; 
		for(int k=0; k<4; ++k)
		    vY[1] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][1]* (k == 0 || k == 3 ? 1.0 : 3.0);
		
		vY[2] = 0; 
		for(int k=0; k<4; ++k)
		    vY[2] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][2]* (k == 0 || k == 3 ? 1.0 : 3.0);

		if( j > 0.0 )
		    objTransform.addChild(createLine(vX,vY));		

		vX[0] = vY[0];
		vX[1] = vY[1];
		vX[2] = vY[2];	    

	    }

	}

	N = listB.size();

	for (int i=0; i<(N-1); ++i) {
	    if(i != 0)
		node[0] = (Nodo) listB.get(i-1);
	    node[1] = (Nodo) listB.get(i);
	    if(min>node[1].y)
		min = node[1].y;
	    node[2] = (Nodo) listB.get(i+1);
	    if(i != N-2)
		node[3] = (Nodo) listB.get(i+2);

	    b[0][0] = node[1].x;
	    b[1][0] = node[1].x + (node[2].x - node[ (i == 0 ? 1 : 0) ].x)*bezier;
	    b[2][0] = node[2].x - (node[ (i == N-2 ? 2 : 3) ].x - node[1].x)*bezier;
	    b[3][0] = node[2].x;

	    b[0][1] = node[1].y;
	    b[1][1] = node[1].y + (node[2].y - node[ (i == 0 ? 1 : 0) ].y)*bezier;
	    b[2][1] = node[2].y - (node[ (i == N-2 ? 2 : 3) ].y - node[1].y)*bezier;
	    b[3][1] = node[2].y;

	    b[0][2] = node[1].z;
	    b[1][2] = node[1].z + (node[2].z - node[ (i == 0 ? 1 : 0) ].z)*bezier;
	    b[2][2] = node[2].z - (node[ (i == N-2 ? 2 : 3) ].z - node[1].z)*bezier;
	    b[3][2] = node[2].z;
	    
	    for(double j=0.0; j <= 1.0; j += partition){	      
		vY[0] = 0; 
		for(int k=0; k<4; ++k)
		    vY[0] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][0] * (k == 0 || k == 3 ? 1.0 : 3.0);
		
		vY[1] = 0; 
		for(int k=0; k<4; ++k)
		    vY[1] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][1]* (k == 0 || k == 3 ? 1.0 : 3.0);
		
		vY[2] = 0; 
		for(int k=0; k<4; ++k)
		    vY[2] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][2]* (k == 0 || k == 3 ? 1.0 : 3.0);

		if( j > 0.0 )
		    objTransform.addChild(createLine(vX,vY));
		
		vX[0] = vY[0];
		vX[1] = vY[1];
		vX[2] = vY[2];		
	    
	    }

	}

	if(isthereN){

	    b[0][0] = sW.x;
	    b[1][0] = sW.x;
	    b[2][0] = sE.x;
	    b[3][0] = sE.x;

	    b[0][1] = sW.y;
	    b[1][1] = sW.y+min*(nE.x-nW.x<1?1:nE.x-nW.x);
	    b[2][1] = sE.y+min*(nE.x-nW.x<1?1:nE.x-nW.x);
	    b[3][1] = sE.y;

	    b[0][2] = sW.z;
	    b[1][2] = sW.z;
	    b[2][2] = sE.z;
	    b[3][2] = sE.z;
	    
	    for(double j=0.0; j <= 1.0; j += partition1){	      
		vY[0] = 0; 
		for(int k=0; k<4; ++k)
		    vY[0] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][0] * (k == 0 || k == 3 ? 1.0 : 3.0);
		
		vY[1] = 0; 
		for(int k=0; k<4; ++k)
		    vY[1] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][1]* (k == 0 || k == 3 ? 1.0 : 3.0);
		
		vY[2] = 0; 
		for(int k=0; k<4; ++k)
		    vY[2] += Math.pow(1.0 - j,3-k) * Math.pow(j,k) * b[k][2]* (k == 0 || k == 3 ? 1.0 : 3.0);

		if( j > 0.0 )
		    objTransform.addChild(createLine(vX,vY));		

		vX[0] = vY[0];
		vX[1] = vY[1];
		vX[2] = vY[2];	    

	    }

	}

	return objTransform;

    }


    public int isNumber(String cadena, int indice){  
    
	if(cadena.length() > indice){
	    if((cadena.charAt(indice) >= '0' && cadena.charAt(indice)<='9'))
		if(cadena.length() > (indice+1)){
		    if((cadena.charAt(indice+1) >= '0' && cadena.charAt(indice+1)<='9')){
			int number = isNumber(cadena,indice+1);
			if(number > indice )
			    return number;
		    }else
			return (indice+1);
		}else
		    return (indice+1);
	    
	    if(cadena.charAt(indice)  == '-')
		if(cadena.length() > (indice+1))
		    if((cadena.charAt(indice+1) >= '0' && cadena.charAt(indice+1)<='9')){
			int number = isNumber(cadena,indice+1);
			if(number > indice )
			    return number;
		    }   	       
	}
	return indice;	
    }

    public int isVector(String cadena, int indice){

	int number = isNumber(cadena,indice);
	if( number > indice ){
	    if( cadena.length() > number ){
		if(cadena.charAt(number)==','){
		    int numberI = isVector(cadena,number+1);
		    if(numberI > number+1 )
			return numberI;
		}else
		    return number;
	    }else
		return number;	    
	}

	return indice;

    }

    public int isSuma(String cadena, int indice){

	int number = isVector(cadena,indice);
	if( number > indice ){
	    if( cadena.length() > number ){
		if(cadena.charAt(number)=='+'){
		    int numberI = isSuma(cadena,number+1);
		    if(numberI > number+1 )
			return numberI;
		}else
		    return number;
	    }else
		return number;	    
	}
	
	return indice;
    }

    public boolean isValid(String cadena){

	int number = isSuma(cadena,0);

	if(number > 0)
	    if(cadena.length() == number)
		return true;
	

	if(cadena.length() > 3){
	    if(cadena.charAt(0) == 'N')
		if(cadena.charAt(1) == '('){
		    number = isSuma(cadena,2);
		    if(number > 2)		
			if(cadena.length()-1 == number && cadena.charAt(number) == ')')
			    return true;
		    
		}
	}
	text.select(number,number+1);
	
	return false;
    }

    public BranchGroup createSceneGraph(String cadena) {
        BranchGroup objRoot = new BranchGroup();

        TransformGroup objTransform = new TransformGroup();
        objTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        objRoot.addChild(objTransform);     
	
	cadena = cadena.trim();
	
	for(int i=1; i<cadena.length(); i++)
	    cadena = cadena.substring(0,i)+cadena.substring(i).trim();
	
	text.setText(cadena);  
			
	if(cadena.length()>0)
	    if(isValid(cadena))
		objTransform.addChild(createKnot(cadena));
	    else{
		Transform3D translate = new Transform3D();	    
		translate.set(new Vector3f((float) -0.14, (float) -0.025, (float) 0.0));
		TransformGroup objTranslate = new TransformGroup(translate);
		objTransform.addChild(objTranslate);
		
		TransformGroup objSpin = new TransformGroup();
		objSpin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objTranslate.addChild(objSpin);
		
		Text2D text2d = new Text2D("Sintaxis Error" , new Color3f(0.0f, 0.0f, 0.0f), "Helvetica", 12, 0);
		
		objSpin.addChild(text2d);
		
		Appearance textAppear = text2d.getAppearance();
		
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setBackFaceNormalFlip(true);
		textAppear.setPolygonAttributes(polyAttrib);
	    }
	else{
	    Transform3D translate = new Transform3D();	    
	    translate.set(new Vector3f((float) -0.15, (float) -0.05, (float) 0.0));
	    TransformGroup objTranslate = new TransformGroup(translate);
	    objTransform.addChild(objTranslate);

	    TransformGroup objSpin = new TransformGroup();
	    objSpin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    objTranslate.addChild(objSpin);
	    
	    Text2D text2d = new Text2D("KnotJ3D" , new Color3f(0.0f, 0.0f, 0.0f), "Helvetica", 24, 0);
	    
	    objSpin.addChild(text2d);
	    
	    Appearance textAppear = text2d.getAppearance();
	    
	    PolygonAttributes polyAttrib = new PolygonAttributes();
	    polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
	    polyAttrib.setBackFaceNormalFlip(true);
	    textAppear.setPolygonAttributes(polyAttrib);
	}

        MouseRotate myMouseRotate = new MouseRotate();
        myMouseRotate.setTransformGroup(objTransform);
        myMouseRotate.setSchedulingBounds(new BoundingSphere());
        objRoot.addChild(myMouseRotate);

        MouseTranslate myMouseTranslate = new MouseTranslate();
        myMouseTranslate.setTransformGroup(objTransform);
        myMouseTranslate.setSchedulingBounds(new BoundingSphere());
        objRoot.addChild(myMouseTranslate);

        MouseZoom myMouseZoom = new MouseZoom();
        myMouseZoom.setTransformGroup(objTransform);
        myMouseZoom.setSchedulingBounds(new BoundingSphere());
        objRoot.addChild(myMouseZoom);

	BoundingSphere bounds = new BoundingSphere();
        bounds.setRadius(1.5);
        myMouseRotate.setSchedulingBounds(bounds);

        DirectionalLight lightD = new DirectionalLight();
        lightD.setInfluencingBounds(bounds);
        lightD.setDirection(new Vector3f(0.0f, 1.0f, -1.0f));
        objRoot.addChild(lightD);
	
	Background background = new Background();
        background.setColor(1.0f, 1.0f, 1.0f);
        background.setApplicationBounds(bounds);
        objRoot.addChild(background);

        objRoot.compile();

	return objRoot;
    } 

    public KnotJ3d () {

        setLayout(new BorderLayout());
	GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

        canvas3D = new Canvas3D(config);

        add("Center", canvas3D);        

        BranchGroup scene = createSceneGraph("");

	simpleU = new SimpleUniverse(canvas3D);

        simpleU.getViewingPlatform().setNominalViewingTransform();

        simpleU.addBranchGraph(scene);  

	text.addActionListener(this);
        add("South", text);
	text.setText("Write Here");
	text.selectAll();    
    }

    public void actionPerformed (ActionEvent event) {
        Object s = event.getSource( );	
	if ( s == text ) {	
	    simpleU.cleanup();

	    String cadena;
	    cadena=text.getText( );
	    BranchGroup scene = createSceneGraph(cadena);

	    simpleU = new SimpleUniverse(canvas3D);
	    simpleU.getViewingPlatform().setNominalViewingTransform();
	    simpleU.addBranchGraph(scene); 
        }	    
    }

    public static void main(String[] args) {          
        System.out.println("\n  Hold the mouse button while moving the mouse to make the figure move.");
        System.out.println("     left mouse button       -  rotate");
        System.out.println("     right mouse button      -  translate");
        System.out.println("     Alt+left mouse button   -  zoom\n");       
        Frame frame = new MainFrame(new KnotJ3d(), 512, 512);
    }

}

    class Nodo {      // class Nodo's start

	public double x;
	public double y;
	public double z;

	public Nodo(){ }

	public boolean equals(Nodo Aux){
	    if(x == Aux.x && y == Aux.y && z == Aux.z)
		return true;	
	    return false;
	}

	public void equalize(Nodo Aux){
	    x=Aux.x;
	    y=Aux.y;
	    z=Aux.z;
	}

	public void rotate90(){
	    double aux=x;
	    x=-y;
	    y=aux;
	}

	public void rotateh(){
	    y=-y;
	}

	public void moveX(double auxInt){	
	    x += auxInt;
	}

	public void print(){
	    System.out.println( x + " " + y + " " + z );
	}
    
    }                 // Class Nodo's end

    class LinkNodo {  // Class LinkNodo's start
	
	public Nodo nE;
	public Nodo nW;
	public Nodo sE;
	public Nodo sW;	
	public LinkedList listA;
	public LinkedList listB;

	LinkNodo(){
	    nE = new Nodo();
	    nW = new Nodo();
	    sE = new Nodo();
	    sW = new Nodo();
	    listA = new LinkedList();
	    listB = new LinkedList();
	}
	
	public void equalize(Nodo auxnE, Nodo auxnW, Nodo auxsE, Nodo auxsW, LinkedList auxlistA, LinkedList auxlistB){
	    
	    nE = auxnE;
	    nW = auxnW;
	    sE = auxsE;
	    sW = auxsW;
	    listA = auxlistA;
	    listB = auxlistB;
	    
	}

    }                 // Class LinkNodo's end

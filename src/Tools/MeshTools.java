package Tools;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;


import WeightFunctions.ALineWeightFilter;
import WeightFunctions.ILineWeightFilter;
import WeightFunctions.LnLineFilter;
import WeightFunctions.PowerLineFilter;

/**
 * 
 */

/**
 * @author Piotr Bugaj
 * @date May 30, 2010
 */
public class MeshTools {



	public MeshTools() {
	}

	/**------------------------------------------------------------------**/
	/**Get the faces touching each edge within the mesh.
	 * Param 1: edges existing within the mesh
	 * Param 2: faces makig up the mesh
	 * Param 3: vertexes of each face**/
	/**------------------------------------------------------------------**/
	/**Return values: 
	 * Key: a1xb2xc3ya2xb2xc2  - corresponding to edge (a1,b1,c1)-(a2,b2,c2)
	 * Value: {f1, f2, ... fk} - corresponding to face numbers touching edge 
	 *                           (a1,b1,c1)-(a2,b2,c2)**/
	/**------------------------------------------------------------------**/
	/**Note: the edges are assumed to be unique**/
	/**------------------------------------------------------------------**/
	public Hashtable<String, int[]> getTouchingFaces(
			Hashtable<String, float[][]> edges, 
			int[][]faces, float [][]vertexes) {

		/**Array storing the edges and the corresponding faces**/
		Hashtable<String, int[]> touchingFaces = new Hashtable<String, int[]>();

		/**Initiallize the hashtable, assigning each edge
		 * an empty integer array as default**/
		Enumeration<float[][]>tempEdges = edges.elements();
		while(tempEdges.hasMoreElements()) {
			float[][] tempVertexes = tempEdges.nextElement();

			touchingFaces.put(edgeXYForm(new float[][]{tempVertexes[0], tempVertexes[1]}),
					new int[]{});
		}

		/**Go through each face and store the nine coordinate values, three
		 * values for the three vertexes making up each face**/
		for(int i = 0; i < faces.length; i++) {

			/**Store the face in a temporary location**/
			int [] tempFaces = faces[i];

			float [] v1 = vertexes[tempFaces[0]-1];
			float [] v2 = vertexes[tempFaces[1]-1];
			float [] v3 = vertexes[tempFaces[2]-1];

			/**String a1 = edge(v1, v2)**/
			String a1 = edgeXYForm(new float[][]{v1, v2});

			/**String a2 = edge(v2, v1)**/
			String a2 = edgeXYForm(new float[][]{v2, v1});

			/**String b1 = edge(v1, v3)**/
			String b1 = edgeXYForm(new float[][]{v1, v3});

			/**String b2 = edge(v3, v1)**/
			String b2 = edgeXYForm(new float[][]{v3, v1});

			/**String c1 = edge(v2, v3)**/
			String c1 = edgeXYForm(new float[][]{v2, v3});

			/**String c2 = edge(v3, v2)**/
			String c2 = edgeXYForm(new float[][]{v3, v2});           


			/**The set of edges is unique. Therefore the set either contains a1
			 * or a2, b1 or b2, c1 or c2**/

			/**Case where edge a1 exists**/
			if(touchingFaces.containsKey(a1)) {
				int [] temp = touchingFaces.get(a1);

				/**Update the array containing the faces touching this edge**/
				touchingFaceHelper(touchingFaces, temp, a1, i);
			}

			/**Case where edge a2 exists instead of edge a1**/
			if(touchingFaces.containsKey(a2)) {
				int [] temp = touchingFaces.get(a2);
				touchingFaceHelper(touchingFaces, temp, a2, i);
			}

			if(touchingFaces.containsKey(b1)) {
				int [] temp = touchingFaces.get(b1);
				touchingFaceHelper(touchingFaces, temp, b1, i);
			}

			if(touchingFaces.containsKey(b2)) {
				int [] temp = touchingFaces.get(b2);
				touchingFaceHelper(touchingFaces, temp, b2, i);
			}

			if(touchingFaces.containsKey(c1)) {
				int [] temp = touchingFaces.get(c1);
				touchingFaceHelper(touchingFaces, temp, c1, i);
			}

			if(touchingFaces.containsKey(c2)) {
				int [] temp = touchingFaces.get(c2);
				touchingFaceHelper(touchingFaces, temp, c2, i);
			}
		}
		return touchingFaces;
	}

	/**Helper method for the function: getTouchingFaces**/
	private int touchingFaceHelper(
			Hashtable<String, int[]> touchingFaces,
			int [] touchingFacesArray,
			String edgeString,
			int faceNumber) {

		int [] newTemp = new int[touchingFacesArray.length + 1];
		for(int j = 0; j < newTemp.length-1; j++) {
			newTemp[j] = touchingFacesArray[j];
		}
		newTemp[newTemp.length-1] = faceNumber;
		touchingFaces.put(new String(edgeString), newTemp);

		return 0;
	}

	/**------------------------------------------------------------------**/
	/**Given a set of triangles in the form int[][], and the
	 * corresponding vertexes, return larger triangles also
	 * in the same form, composed of four smaller triangles**/
	/**------------------------------------------------------------------**/
	public int [][] getBiggerTriangles(int[][] faces, float[][]vertexes) {

		Hashtable<String, float[][]> edges =
			findAllEdges(vertexes, faces);
		Hashtable<String, int[]>  touchingFaces =
			getTouchingFaces(edges, faces, vertexes);
		int [][] faceNeighbours =
			getFaceNeighbours(vertexes, faces, touchingFaces);

		Hashtable<String, Boolean>
		markedFaces = new Hashtable<String, Boolean>();
		for(int i = 0; i < faces.length; i++) {
			int []tempFace = faces[i];
			
			float[] v1 = vertexes[tempFace[0]-1];
			float[] v2 = vertexes[tempFace[1]-1];
			float[] v3 = vertexes[tempFace[2]-1];
			
			markedFaces.put(this.getCentroid(v1, v2, v3), false);
		}

		Vector<int[]> newFacesVector = new Vector<int[]>();
		for(int i = 0; i < faces.length; i++) {
			int []tempFace = faces[i];
			
			float[] v1 = vertexes[tempFace[0]-1];
			float[] v2 = vertexes[tempFace[1]-1];
			float[] v3 = vertexes[tempFace[2]-1];
			
			if(markedFaces.get(getCentroid(v1, v2, v3))) {
				continue;
			} else {
				markedFaces.put(getCentroid(v1, v2, v3), true);
			}
			
			int [] newFace = getNewFaceHelper(
					tempFace, markedFaces, 
					faceNeighbours, i, faces, vertexes);

			newFacesVector.add(newFace);
		}
		int [][] newFaces = new int[newFacesVector.size()][3];
		newFacesVector.toArray(newFaces);
		
		return newFaces;
	}

	private int [] getNewFaceHelper(
			int [] tempFace,
			Hashtable<String, Boolean> markedFaces,
			int [][] faceNeighbours,
			int faceNumber,
			int[][] faces,
			float [][]vertexes) {
		
		/**Get the neighbouring faces of the current face, tempFace,
		 * given  the  current face's  corresponding face  number**/
		int [] tempNeighbours = faceNeighbours[faceNumber];
		
		/**If there is not enough triangles to construct a new triangle
		 * of  a  size of four  smaller original  triangles,  return**/
		for(int i = 0; i < 3; i++) {
			
			int n1 = tempNeighbours[i];
			if(n1 == -1) {
				return tempFace;
			}
		}
		
		/**Ensure  that   no  neighbours  are  marked
		 * (already belong to some other triangle)**/
		for(int i = 0; i < 3; i++) {
			int n1 = tempNeighbours[i];

			float [] v1 = vertexes[faces[n1][0]-1];
			float [] v2 = vertexes[faces[n1][1]-1];
			float [] v3 = vertexes[faces[n1][2]-1];
			
			if(markedFaces.get(getCentroid(v1, v2, v3))) {
				return tempFace;
			}
		}
		
		/**If  none   of the   faces   have   been
		 * marked,  marked them   as   they   will
		 * now be constructed with the triangle**/
		for(int i = 0; i < 3; i++) {
			int n1 = tempNeighbours[i];
			
			float [] v1 = vertexes[faces[n1][0]-1];
			float [] v2 = vertexes[faces[n1][1]-1];
			float [] v3 = vertexes[faces[n1][2]-1];
			
			markedFaces.put(getCentroid(v1, v2, v3), true);
		}		
		
		/**Get all the corresponding vertexes, for the
		 * center  triangle,   and  its  neighbours**/
		int n1 = tempNeighbours[0];
		
		float [] v1_n1 = vertexes[faces[n1][0]-1];
		float [] v2_n1 = vertexes[faces[n1][1]-1];
		float [] v3_n1 = vertexes[faces[n1][2]-1];

		int n2 = tempNeighbours[1];
		
		float [] v1_n2 = vertexes[faces[n2][0]-1];
		float [] v2_n2 = vertexes[faces[n2][1]-1];
		float [] v3_n2 = vertexes[faces[n2][2]-1];
		
		int n3 = tempNeighbours[2];
		
		float [] v1_n3 = vertexes[faces[n3][0]-1];
		float [] v2_n3 = vertexes[faces[n3][1]-1];
		float [] v3_n3 = vertexes[faces[n3][2]-1];

		float [] v1_cen = vertexes[tempFace[0]-1];
		float [] v2_cen = vertexes[tempFace[1]-1];
		float [] v3_cen = vertexes[tempFace[2]-1];
		
		/**The new face to be constructed**/
		int [] newFace = new int[3];
		int counter = 0;
		
		if((v1_n1 != v1_cen) && (v1_n1 != v2_cen) && (v1_n1 != v3_cen)) {
			newFace[counter] = faces[n1][0];
			counter++;
		}
		if((v2_n1 != v1_cen) && (v2_n1 != v2_cen) && (v2_n1 != v3_cen)) {
			newFace[counter] = faces[n1][1];
			counter++;
		}		
		if((v3_n1 != v1_cen) && (v3_n1 != v2_cen) && (v3_n1 != v3_cen)) {
			newFace[counter] = faces[n1][2];
			counter++;
		}			
		

		if((v1_n2 != v1_cen) && (v1_n2 != v2_cen) && (v1_n2 != v3_cen)) {
			newFace[counter] = faces[n2][0];
			counter++;
		}
		if((v2_n2 != v1_cen) && (v2_n2 != v2_cen) && (v2_n2 != v3_cen)) {
			newFace[counter] = faces[n2][1];
			counter++;
		}		
		if((v3_n2 != v1_cen) && (v3_n2 != v2_cen) && (v3_n2 != v3_cen)) {
			newFace[counter] = faces[n2][2];
			counter++;
		}	
		

		if((v1_n3 != v1_cen) && (v1_n3 != v2_cen) && (v1_n3 != v3_cen)) {
			newFace[counter] = faces[n3][0];
			counter++;
		}
		if((v2_n3 != v1_cen) && (v2_n3 != v2_cen) && (v2_n3 != v3_cen)) {
			newFace[counter] = faces[n3][1];
			counter++;
		}		
		if((v3_n3 != v1_cen) && (v3_n3 != v2_cen) && (v3_n3 != v3_cen)) {
			newFace[counter] = faces[n3][2];
			counter++;
		}	
		
		if(counter != 3) {
			System.out.println("Ouch");
			System.exit(1);
		}
		
		return newFace;
	}
	

	/**Given trailing faces and composed line bases, return the composed line
	 * bases  that  contain at least one  triangle that is a trailing face**/
	public Vector<int[][]> getTrailingBases(
			Vector<int[][]> composedLineBases,
			Hashtable<String,Boolean> trailingFaces,
			float[][]vertexes,
			int[][] faces) {

		Vector<int[][]> trailingBases = new Vector<int[][]>();

		Enumeration<int[][]> enumm = composedLineBases.elements();
		while(enumm.hasMoreElements()) {
			int[][] base = enumm.nextElement();
			Vector<int[]> newBase = new Vector<int[]>();

			/**Itertate     through    the     triangles
			 * belonging  in  the  base  and  find   the
			 * ones that face the direction of motion**/
			for(int i = 0; i < base.length; i++) {
				int [] face_i = base[i];

				float [] v1 = vertexes[face_i[0]-1];
				float [] v2 = vertexes[face_i[1]-1];
				float [] v3 = vertexes[face_i[2]-1];

				if(trailingFaces.containsKey(getCentroid(v1, v2, v3))) {
					newBase.add(face_i);
				}
			}
			if(newBase.size() > 0) {
				int [][] newBaseArrayForm = new int[newBase.size()][];
				newBase.toArray(newBaseArrayForm);
				trailingBases.add(newBaseArrayForm);
			}
		}



		return trailingBases;
	}

	/**Given  a set  of triangles  and the corresponding vertexes,  return
	 * a set of triangular  bases of a given size.   A triangular base  is
	 * made up of triangles, with the resulting shape being a triangles**/
	public float[][] getTriangularBases(int size) {



		return null;
	}

	/**Go   through the  list of  provided edges  and collect  the
	 * ones that only have one face touching it (boundary face)**/
	public Hashtable<String, float [][]> getBoundaryEdges(
			Hashtable<String, float[][]> baseEdges,
			Hashtable<String, int[]> touchingBaseFaces) {	

		Hashtable<String, float [][]> boundaryEdges =
			new Hashtable<String, float [][]>();

			Enumeration<float[][]> baseEdgesEnum = baseEdges.elements();
			while(baseEdgesEnum.hasMoreElements()) {
				float [][] temporaryEdge = baseEdgesEnum.nextElement();

				/**Get the touching faces for this edge**/
				String n1 = edgeXYForm(
						new float[][]{temporaryEdge[0],temporaryEdge[1]});
				String n2 = edgeXYForm(
						new float[][]{temporaryEdge[1],temporaryEdge[0]});

				int []temporaryTouchingFaces = 
					touchingBaseFaces.get(n1);
				int []temporaryTouchingFaces2 = 
					touchingBaseFaces.get(n2);

				if(temporaryTouchingFaces != null &&
						temporaryTouchingFaces.length == 1) {
					boundaryEdges.put(n1, temporaryEdge);
				}

				/**Get the touching faces for this edge**/
				else if(temporaryTouchingFaces2 != null &&
						temporaryTouchingFaces2.length == 1) {
					boundaryEdges.put(n2, temporaryEdge);
				}
			}

			return boundaryEdges;
	}

	/**------------------------------------------------------------------**/
	/**Description: Given a set of faces in the form: float[][][],
	 * return the vertexes making up those triangles**/
	/**------------------------------------------------------------------**/
	public float[][] getTriangleVertexes(float[][][] triangles) {



		return null;
	}

	/**------------------------------------------------------------------**/
	/**Get all the neighbouring faces for each face.
	 * Param 1: vertexes makig up each face
	 * Param 2: faces making uup the mesh
	 * Param 3: set of edges along with the faces that touch them**/
	/**------------------------------------------------------------------**/
	/**Return values: 
	 * [face number][f1, f2, f3] **/
	/**------------------------------------------------------------------**/
	/**Note: each face is only expected to have three neighbours**/
	/**------------------------------------------------------------------**/
	public int [][] getFaceNeighbours(float [][] vertexes,  int [][] faces, 
			Hashtable<String, int[]> touchingFaces) {

		/**Variable containing the face neighbours for each face. Each face is
		 * assumed to have at most tree faces**/
		int [][] faceNeighbours = new int[faces.length][3];
		for(int i = 0; i < faceNeighbours.length; i++) {

			/**Initialize the values to negative -1.
			 * -1 indicating that no neighbour exists**/
			/**Note: three neighbours are expected per triangle.
			 * I.e., one  triangle  is attached  to each side**/
			faceNeighbours[i] = new int[]{-1, -1, -1};
		}

		/**Go through each face and store the nine coordinate values, three
		 * values for the three vertexes making up each face**/
		for(int i = 0; i < faces.length; i++) {

			/**Store the face in a temporary location**/
			int [] tempFaces = faces[i];

			float [] v1 = vertexes[tempFaces[0]-1];
			float [] v2 = vertexes[tempFaces[1]-1];
			float [] v3 = vertexes[tempFaces[2]-1];

			/**String a1 = edge(v1, v2)**/
			String a1 = edgeXYForm(new float[][]{v1, v2});

			/**String a2 = edge(v2, v1)**/
			String a2 = edgeXYForm(new float[][]{v2, v1});

			/**String b1 = edge(v1, v3)**/
			String b1 = edgeXYForm(new float[][]{v1, v3});

			/**String b2 = edge(v3, v1)**/
			String b2 = edgeXYForm(new float[][]{v3, v1});

			/**String c1 = edge(v2, v3)**/
			String c1 = edgeXYForm(new float[][]{v2, v3});

			/**String c2 = edge(v3, v2)**/
			String c2 = edgeXYForm(new float[][]{v3, v2});        

			/**Once the edges for face i is stored, for each edge finding the
			 * faces that are touching it**/
			/**If the face p != i, i.e., the face touching the edge is some other
			 * face then the current face i being iterated, add that face number
			 * to the array of face neighbours corresponding to face i**/
			if(touchingFaces.containsKey(a1)) {

				/**Get the touching faces for edge a1**/
				int [] tempTouchingFaces = touchingFaces.get(a1);		

				/**Find the  touching face that  is a neighbour of
				 * the triangle and add it to its neighbourhood**/
				getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
			}

			if(touchingFaces.containsKey(a2)) {
				int [] tempTouchingFaces = touchingFaces.get(a2);				
				getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
			}

			if(touchingFaces.containsKey(b1)) {
				int [] tempTouchingFaces = touchingFaces.get(b1);				
				getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
			}

			if(touchingFaces.containsKey(b2)) {
				int [] tempTouchingFaces = touchingFaces.get(b2);				
				getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
			}

			if(touchingFaces.containsKey(c1)) {
				int [] tempTouchingFaces = touchingFaces.get(c1);				
				getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
			}

			if(touchingFaces.containsKey(c2)) {
				int [] tempTouchingFaces = touchingFaces.get(c2);				
				getFaceNeighboursHelper(faceNeighbours, tempTouchingFaces, i);
			}

		}

		return faceNeighbours;
	}

	/**Helper function for the method: getFaceNeighbours**/
	private int getFaceNeighboursHelper(
			int [][] faceNeighbours,
			int [] tempTouchingFaces, int i) {

		/**Iterate through these faces**/
		for(int p = 0; p < tempTouchingFaces.length; p++) {

			/**Case where a face is found touching the edge of face i that is not
			 * face i itself**/
			if(tempTouchingFaces[p]!= i) {
				/**Iterate through the current neighbouring faces of face i
				 * until an empty slot is found. Note that only three
				 * space are expected to be filled**/
				for(int z = 0; z < faceNeighbours[i].length; z++) {
					if(faceNeighbours[i][z] == -1) {
						faceNeighbours[i][z] = tempTouchingFaces[p];
						return 0;
					}
				}
			}
		}

		return 0;
	}

	/**------------------------------------------------------------------**/
	/**Get all the neighbouring vertexes for each vertex.
	 * Param 1: The edges existing within the mesh**/
	/**------------------------------------------------------------------**/
	/** Return values:
	 * KEY: "a b c", VALUE: {vertexes connected to (a, b, c) **/
	/**------------------------------------------------------------------**/
	public Hashtable<String, float[][]> getVertexNeighbours(Hashtable<String,
			float[][]> edges) {

		/**Create the hash table for storing the neighbours**/
		/**KEY: "a b c", VALUE: {vertexes connected to (a, b, c) **/
		Hashtable <String, float[][]>vertexNeighbours =
			new Hashtable <String, float[][]>();

			/**Enumerate through the edges. For each edge (v1)-(v2), check
			 * if v1 or v2 already exists  within the  hash table.  If one
			 * already exists,  check if  that vertex has the other vertex
			 * already  as  a  neighbour, i.e., it exists within the array
			 * corresponding to that vertex.
			 * 
			 * Then add the  vertex to  that array depending on whether or
			 * not the vertex yet exists within the array**/
			Enumeration<float[][]> enumEdge = edges.elements();
			while(enumEdge.hasMoreElements()) {

				float [][]tempEdge = enumEdge.nextElement();

				/**Case where tempEdge[0] already exist in the hash table**/
				if(vertexNeighbours.containsKey(vForm(tempEdge[0]))) {

					/**Store the current neighbours for the vertex**/
					float[][] tempNeighbours = vertexNeighbours.get(vForm(tempEdge[0]));

					/**Array storing the new set of neighbours with one additional
					 * vertex, tempEdge[1]**/
					int tempLength =  tempNeighbours.length;
					float [][] newNeighbours = new float[tempLength + 1][3];

					/**Indicating whether the neighbouring vertex tempEdge[1]
					 * exists for the vertex tempEdge[0]**/
					boolean neighbourExists = false;
					this_loop: for(int i = 0; i < tempLength; i++) {
						if(tempNeighbours[i] == tempEdge[1]) {
							neighbourExists = true;
							break this_loop;
						}
						newNeighbours[i] = tempNeighbours[i];
					}

					/**At the second vertex if it is not yet a neighbour of the
					 * first vertex**/
					if(!neighbourExists) {
						newNeighbours[tempLength] = tempEdge[1];
						vertexNeighbours.put(vForm(tempEdge[0]), newNeighbours);
					}

					/**Case where the first vertex does not yet exist within the hash table**/
				} else if(!vertexNeighbours.containsKey(vForm(tempEdge[0]))) {
					vertexNeighbours.put(vForm(tempEdge[0]), new float[][]{tempEdge[1]});
				}


				if(vertexNeighbours.containsKey(vForm(tempEdge[1]))) {
					/**Store the current neighbours for the vertex**/
					float[][] tempNeighbours =
						vertexNeighbours.get(vForm(tempEdge[1]));

					/**Array storing the new set of neighbours with one additional
					 * vertex, tempEdge[1]**/
					int tempLength =  tempNeighbours.length;
					float [][] newNeighbours = new float[tempNeighbours.length + 1][3];

					/**Indicating whether the neighbouring vertex tempEdge[1]
					 * exists for the vertex tempEdge[0]**/
					boolean neighbourExists = false;
					this_loop: for(int i = 0; i < tempLength; i++) {
						if(tempNeighbours[i] == tempEdge[0]) {
							neighbourExists = true;
							break this_loop;
						}
						newNeighbours[i] = tempNeighbours[i];
					}
					if(!neighbourExists) {
						newNeighbours[tempLength] = tempEdge[0];
						vertexNeighbours.put(vForm(tempEdge[1]), newNeighbours);
					}
				} else if(!vertexNeighbours.containsKey(vForm(tempEdge[1]))) {
					vertexNeighbours.put(vForm(tempEdge[1]), new float [][]{tempEdge[0]});
				}

			}

			return vertexNeighbours;
	}

	/**------------------------------------------------------------------**/
	/**Find the difference between two lines
	 * corresponding  to the  given  weights
	 * 
	 * Param 1: First line.
	 * Param 2: Second line.
	 * Param 3: Weight relating to the difference between the two lines
	 * Param 4: Name of the function distributing the weight
	 * **/
	/**------------------------------------------------------------------**/
	/**Return value: Difference between the two lines**/
	/**------------------------------------------------------------------**/
	public float getLineDifference(
			float [][] line1, float [][]line2,
			float []params,
			String weightFunction) {

		float diff = 0.0f;
		ILineWeightFilter lf = null;
		float [] weightValues;

		/**Get the weighted values if required**/
		if(weightFunction.equals(ALineWeightFilter.LN)) {
			lf = new LnLineFilter();
		} else if(weightFunction.equals(ALineWeightFilter.POW)) {
			lf = new PowerLineFilter();
		}

		lf.setParams(params);
		lf.calculateLineFunction(line1.length);
		weightValues = lf.getLineFunction();

		/**Calculate the difference**/
		if(weightFunction.equals(ALineWeightFilter.NONE)) {
			for(int i = 0; i < line1.length; i++) {
				diff += Math.sqrt(
						Math.pow(line1[i][0] + line2[i][0], 2) +
						Math.pow(line1[i][1] + line2[i][1], 2) +
						Math.pow(line1[i][2] + line2[i][2], 2));
			}
		} else {
			for(int i = 0; i < line1.length; i++) {
				diff += (Math.sqrt(
						Math.pow(line1[i][0] + line2[i][0], 2) +
						Math.pow(line1[i][1] + line2[i][1], 2) +
						Math.pow(line1[i][2] + line2[i][2], 2)))
						*
						weightValues[i];
			}
		}

		/**Return the average**/
		diff = diff/(line1.length + 0.0f);
		return diff;
	}

	/**------------------------------------------------------------------**/
	/**Find all the edges within the mesh.
	 * Param 1: the vertexes making up the faces
	 * Param 2: the faces making up the mesh**/
	/**------------------------------------------------------------------**/
	/**Return value: Hashtable for finding all the edges.
	 * KEY: "(a+d, b+e, c+f)", VALUE: (a,b,c), (d,e,f)**/
	/**------------------------------------------------------------------**/
	public Hashtable<String, float[][]> findAllEdges(float vertexes[][],
			int faces[][]) {

		/**Hashtable for finding all the edges.
		 * KEY: "(a+d, b+e, c+f)", VALUE: (a,b,c), (d,e,f)**/
		Hashtable <String, float[][]>edges =
			new Hashtable <String, float[][]>();

			/**Fill up the hash-table**/
			for(int i = 0; i < faces.length; i++) {

				int [] tempFace = faces[i];
				float []v1 = vertexes[tempFace[0]-1];
				float []v2 = vertexes[tempFace[1]-1];
				float []v3 = vertexes[tempFace[2]-1];		

				/**Note that the vertex addition is stored, as oppose  to a string
				 * of representing the coordinates of two vertexes.This is because
				 * the resulting  string is  shorter to  compare.  Also it is much
				 * more work checking  whether the  hash table  contains the  edge
				 * (a, b, c) (d e f) or the flipped edge (d e f) (a b c)**/
				if(!edges.containsKey(edgeXYForm(new float[][]{v1, v2}))  &&
						!edges.containsKey(edgeXYForm(new float[][]{v2, v1}))) {

					edges.put(edgeXYForm(new float[][]{v1, v2}), 
							new float[][]{v1.clone(), v2.clone()});
				} 

				if(!edges.containsKey(edgeXYForm(new float[][]{v2, v3})) &&
						!edges.containsKey(edgeXYForm(new float[][]{v3, v2}))) {

					edges.put(edgeXYForm(new float[][]{v2, v3}), 
							new float[][]{v2.clone(), v3.clone()});
				}			

				if(!edges.containsKey(edgeXYForm(new float[][]{v1, v3})) &&
						!edges.containsKey(edgeXYForm(new float[][]{v3, v1}))) {

					edges.put(edgeXYForm(new float[][]{v1, v3}), 
							new float[][]{v1.clone(), v3.clone()});
				}
			}

			return edges;
	}

	/**------------------------------------------------------------------**/
	/**Given a set of edges, return the triangles that the edges form
	 * Param 1: the set of edges
	 * Param 2: the set of vertexes**/
	/**------------------------------------------------------------------**/
	/**Return value: the set of faces that are created from those edges
	 * 
	 * Description: The algorithm will  work as  follows:  It will do a
	 * breath  first search  on the  edges and store all the neighbours
	 * corresponding to each vertex during the search.   Then given the
	 * neighbours for  each vertex,  triangles will be constructed from
	 * all  the possible  pair of  vertexes  from each neighbourhood of
	 * vertexes.  This algorithm is very similar to just simply storing
	 * all the possible neighbouring vertexes for each vertex, and then
	 * trying out all the  possible  pairs of  vertexes for each vertex
	 * that form a triangle.The only difference is that by using breath
	 * first search,  the total  sum of the  neighbours stored for each
	 * vertex is equal to the total number of edges, as oppose to  just
	 * simply storing all the neighbouring vertexes for each vertex.
	 * 
	 * Runtime: Linear with respect to the number of triangles.
	 */
	/**------------------------------------------------------------------**/
	public Hashtable<String, float[][]> getComposedFaces(
			Hashtable<String, float[][]> edges,
			Hashtable<String, float[][]> neighbouringVertexes,
			float [][]vertexes,
			Hashtable<String, float[]> triangleVertexes) {

		/**Table for storing the created faces by centroid**/
		Hashtable<String, float[][]> composedFaces  =
			new Hashtable<String, float[][]>();

			if((neighbouringVertexes.size() != vertexes.length) ||
					(neighbouringVertexes.size() == 0) ||
					(edges.size() == 0)) {
				System.out.println("Invalid number of vertexes");
				System.exit(1);
				return composedFaces;
			}

			/**A queue for traversing the edges in Bread-First-Search**/
			LinkedList <float[]>queue = new LinkedList<float[]>();

			/**Hashtable indicating which edge have been marked**/
			/**String: (a+d, b+e, c+f), where (a,b,c)-(d,e,f) is an edge**/
			Hashtable <String, Boolean> markedEdges =
				new Hashtable <String, Boolean>();

			/**Hashtable indicating the vertexes attached to a given vertex**/
			Hashtable <String, Vector<float[]>> neighbouringSearchedVertexes =
				new Hashtable <String, Vector<float[]>>();

			/**Push the first vertex onto the stack of visited vertexes**/
			queue.push(vertexes[0]);

			/**Search through the edges until there are no more new ones to be
			 * found**/
			while(!queue.isEmpty()) {

				/**Pop the next visited vertex on the stack**/
				float [] nextVertex = queue.pop();

				/**Get the neighbours for that vertex**/
				float [][] nextVertexNeighbours =
					neighbouringVertexes.get(vForm(nextVertex));

				/**Checked if this vertex has any  neighbouring searched vertexes.
				 * If not,  add them  manually as  edges will  come out  from this
				 * vertex  and no  neighbouring  vertexes  for this  edge  will be
				 * stored otherwise.**/	
				if(!neighbouringSearchedVertexes.containsKey(vForm(nextVertex))){

					/**
                Vector<float[]> temp = new Vector<float[]>();
                for(int i = 0; i < nextVertexNeighbours.length; i++) {
                    temp.add(nextVertexNeighbours[i].clone());
                }
                neighbouringSearchedVertexes.put(vForm(nextVertex), temp);
					 **/
				}


				/**Iterate through the neighbouring vertexes**/
				for(int i = 0; i < nextVertexNeighbours.length; i++) {

					/**Check if a given neighbouring vertex is reached by an
					 * edge that is not yet marked.  (I.e., if breadth first
					 * search can expand on this vertex)**/

					if(!markedEdges.containsKey(edgeXYForm(
							new float[][]{nextVertex, nextVertexNeighbours[i]}))
							&&
							!markedEdges.containsKey(edgeXYForm(
									new float[][]{nextVertexNeighbours[i], nextVertex}))
					){

						/**Mark and push the vertex onto the queue**/
						markedEdges.put(edgeXYForm(
								new float[][]{nextVertex, nextVertexNeighbours[i]}),
								true);
						queue.push(nextVertexNeighbours[i]);

						/**Store the edge newVertex-nextVertexNeighbour as:
						 * nextVertexNeighbour - {newVertex}, if
						 * nextVertexNeighbourdoesn't yet exist in the hashtable.
						 * Otherwise add newVertex to the rest of the stored vertex
						 * neighbours for nextVertexNeighbour.**/
						if(neighbouringSearchedVertexes.containsKey(vForm(nextVertexNeighbours[i]))) {

							Vector <float[]> searchedVertexes =
								neighbouringSearchedVertexes.get(vForm(nextVertexNeighbours[i]));

							searchedVertexes.add(nextVertex.clone());
							neighbouringSearchedVertexes.put(vForm(nextVertexNeighbours[i]), searchedVertexes);
						} else {
							Vector<float[]> temp = new Vector<float[]>();
							temp.add(nextVertex.clone());
							neighbouringSearchedVertexes.put(vForm(nextVertexNeighbours[i]), temp);
						}
					}
					/**End if**/
				}
				/**End for-loop**/

			}

			/**Iterate through the stored vertex neighbours for each vertex and
			 * create the triangles**/
			for(int i = 0; i < vertexes.length; i++) {
				Vector<float[]> tempNeighbours = neighbouringSearchedVertexes.get(vForm(vertexes[i]));

				/**To see where the neighbours at a verex form a triangle, at least
				 * three vertexes must exist (at least two neighbours)**/
				if(tempNeighbours != null && tempNeighbours.size() >= 2) {

					float[][] arrayStor = new float[tempNeighbours.size()][3];
					tempNeighbours.toArray(arrayStor);

					/**Iterate through all the possible combination of neighbours**/
					for(int j = 0; j < arrayStor.length; j++) {
						for(int k = (j+1); k < arrayStor.length; k++) {

							/**Check if the  two  vertexes
							 * are connected by an edge**/
							float [] v1 = arrayStor[j];
							float [] v2 = arrayStor[k];

							if(edges.containsKey(edgeXYForm(new float[][]{v1, v2}))
									||
									edges.containsKey(edgeXYForm(new float[][]{v2, v1})))
							{

								composedFaces.put(this.getCentroid(vertexes[i], v1, v2),

										new float [][]{vertexes[i].clone(), v1, v2});

								/**Make sure  all possible  vertexes are  added as
								 * the  searched  neighbours  might  contain  less
								 * neighbours then in  the  actual  neighbourhood.
								 * Hence  not all  vertexes  will be  collected if
								 * they are just iterated through.**/
								addVertexToTable(triangleVertexes, vertexes[i]);
								addVertexToTable(triangleVertexes, v1);
								addVertexToTable(triangleVertexes, v2);
							}
						}
					}
				}
			}

			return composedFaces;
	}

	/**------------------------------------------------------------------**/
	/**Given a set of vertexes, return a correspondance to their index
	 * Param 1: the set of vertexes
    /**------------------------------------------------------------------**/
	/**Return value: Hashtable:
	 * KEY: vertex (a b c). VALUE: vertex index (i) 
	 **/
	/**------------------------------------------------------------------**/
	public Hashtable <String, Integer> createVertexToIndexCorrespondance(
			float [][] vertexes) {
		Hashtable <String, Integer> vertexToIndex = new Hashtable <String, Integer>();

		for(int i = 0; i < vertexes.length; i++) {
			vertexToIndex.put(vForm(vertexes[i]), i);
		}
		return vertexToIndex;
	}

	/**------------------------------------------------------------------**/	
	/**Given a  triangle in the  form of  vertexes, v1, v2 and v3,  return the
	 * centroid in String form**/
	/**------------------------------------------------------------------**/	
	/** Note:A triangle will have multiple values for it's centroid due to how
	 * the vertexes are added and of how the values  are rounded of.  Hence if
	 * the triangles are accessed by just  the centroid itself, triangles will
	 * fail to be found. **/
	/**------------------------------------------------------------------**/	
	public String getCentroid(float[]v1, float[]v2, float[]v3) {
		float[] centroid = new float[]{
				(  Math.round(v1[0]*10000) +  Math.round(v2[0]*10000) +  Math.round(v3[0]*10000))/3,
				(  Math.round(v1[1]*10000) +  Math.round(v2[1]*10000) +  Math.round(v3[1]*10000))/3,
				(  Math.round(v1[2]*10000) +  Math.round(v2[2]*10000) +  Math.round(v3[2]*10000))/3};

		return new String(centroid[0] +" "+ centroid[1] +" "+ centroid[2]);
	}

	public void addVertexToTable(Hashtable<String, float[]> table, float[]v) {
		table.put(vForm(v), 
				new float[]{v[0],v[1],v[2]});
	}

	/**Given a set of facess, return a vertex to index correspondance,
	 * where the index is two dimensional for  searching with a set of
	 * faces, as oppose to a one dimensional array of vertexes 
	 */
	/** Param 1: the set of faces**/
	/**------------------------------------------------------------------**/
	/**Return value: Hashtable:
	 * KEY: vertex (a b c). VALUE: face index (i)(j) 
	 **/
	public Hashtable <String, int[]> createVertexToIndexCorrespondance(
			float [][][] faces) {
		Hashtable<String,int[]> vertexToIndex =
			new Hashtable <String,int[]>();

			for(int i = 0; i < faces.length; i++) {
				for(int j = 0; j < faces[i].length; j++) {
					vertexToIndex.put(vForm(faces[i][j]), new int[]{i, j});
				}
			}
			return vertexToIndex;
	}

	/**------------------------------------------------------------------**/	
	/**Given a set of faces, return a correspondance to their index
	 * Param 1: the set of faces
    /**------------------------------------------------------------------**/
	/**Return value: Hashtable:
	 * KEY: centroid of triangle (a b c). VALUE: Face index (i)
	 * 
	 * Note: A triangle will have multiple values for it's centroid due to how
	 * the vertexes are added and of how the values  are rounded of.  Hence if
	 * the triangles are accessed by just  the centroid itself, triangles will
	 * fail to be found.
	 **/

	/**------------------------------------------------------------------**/
	public Hashtable<String, Integer> createFacesToIndexCorrespondance(
			float [][] vertexes,
			int [][] faces) {
		Hashtable<String, Integer> faceToIndex = new Hashtable<String, Integer>();

		for(int i = 0; i < faces.length; i++) {
			int [] face = faces[i];

			float [] v1 = vertexes[face[0]-1];
			float [] v2 = vertexes[face[1]-1];
			float [] v3 = vertexes[face[2]-1];

			faceToIndex.put(getCentroid(v1, v2, v3), i);
		}		
		return faceToIndex;
	}

	/**Given a vertex, v = (a, b, c), return it in string form: a b c**/
	public String vForm(float []v) {
		return new String(
				v[0] +" "+ v[1] +" "+ v[2]);
	}

	/**Given  an  edge,  e =  (a, b, c)-(d, e, f),  return  it  in the  string
	 * form: a|||b|||c|||d|||e|||f**/
	public String edgeXYForm(float [][]e) {
		return new String(
				(e[0][0])+"|||"+(e[0][1])+"|||"+(e[0][2])+"|||"+
				(e[1][0])+"|||"+(e[1][1])+"|||"+(e[1][2]));
	}
}
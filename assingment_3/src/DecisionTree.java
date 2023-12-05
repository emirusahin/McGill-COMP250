import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split
	
	// Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;
	
	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf

		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}

		
		// this method takes in a datalist (ArrayList of type datum). It returns the calling DTNode object 
		// as the root of a decision tree trained using the datapoints present in the datalist variable and minSizeDatalist.
		// Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist) {

			if (datalist != null) {
				// Check for empty datalist or small size
				if (datalist.size() >= DecisionTree.this.minSizeDatalist) {
					// Check for homogeneity in labels
					boolean oneTypeLabel = true;
					int datasetLabel = datalist.get(0).y;
					for (Datum datum : datalist) {
						if (datum.y != datasetLabel) {
							oneTypeLabel = false;
							break;
						}
					}

					if (oneTypeLabel) {
						DTNode leafNode = new DTNode();
						leafNode.leaf = true;
						leafNode.label = datasetLabel;
						return leafNode;
					} else {
						// Finding the best attribute and threshold
						double best_avg_entropy = Double.POSITIVE_INFINITY;
						int best_attribute = -1;
						double best_threshold = -1;
						ArrayList<Datum> data1 = new ArrayList<>();
						ArrayList<Datum> data2 = new ArrayList<>();
						double datalistEntropy = calcEntropy(datalist);

						for (int j = 0; j < datalist.get(0).x.length; j++) {
							for (Datum splitDatum : datalist) {
								ArrayList<Datum> datalist1 = new ArrayList<>();
								ArrayList<Datum> datalist2 = new ArrayList<>();

								for (Datum datum : datalist) {
									if (datum.x[j] < splitDatum.x[j]) {
										datalist1.add(datum);
									} else {
										datalist2.add(datum);
									}
								}

								double omegaD1 = (double) datalist1.size() / datalist.size();
								double omegaD2 = (double) datalist2.size() / datalist.size();
								double current_avg_entropy = (calcEntropy(datalist1) * omegaD1
										+ calcEntropy(datalist2) * omegaD2);
								if (current_avg_entropy < best_avg_entropy) {
									best_avg_entropy = current_avg_entropy;
									best_attribute = j;
									best_threshold = splitDatum.x[j];
									data1 = (datalist1);
									data2 = (datalist2);
								}
							}
						}

						if (datalistEntropy != best_avg_entropy) {
							// Create a new node and store the attribute and the threshold
							DTNode newNode = new DTNode();

							if (data1.isEmpty() || data2.isEmpty()) {
								newNode.leaf = true;
								newNode.findMajority(datalist);
								newNode.attribute = -1;
								newNode.left = null;
								newNode.right = null;
								return newNode;
							}

							newNode.attribute = best_attribute;
							newNode.threshold = best_threshold;
							newNode.leaf = false;
							newNode.left = fillDTNode(data1);
							newNode.right = fillDTNode(data2);
							return newNode;
						} else {
							this.leaf = true;
							this.label = findMajority(datalist);
							return this;
						}

					}

				} else {
					DTNode leafNode = new DTNode();
					leafNode.leaf = true;
					leafNode.label = findMajority(datalist);
					return leafNode;
				}
			}
			this.leaf = true;
			this.label = -1;
			return this;
		}


		// This is a helper method. Given a datalist, this method returns the label that has the most
		// occurrences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist) {
			
			int [] votes = new int[2];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}
			
			if (votes[0] >= votes[1])
				return 0;
			else
				return 1;
		}




		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {

			// If this is a leaf node, return its label
			if (this.leaf) {
				return this.label;
			}

			// If this is not a leaf node, determine whether to go left or right
			// based on the threshold and the value of the attribute in xQuery
			if (xQuery[this.attribute] < this.threshold) {
				// Go to the left child
				return this.left.classifyAtNode(xQuery);
			} else {
				// Go to the right child
				return this.right.classifyAtNode(xQuery);
			}
		}


		public boolean equals(Object dt2) {

			//ADD CODE HERE
//			// traversal from both nodes' should give the same result
//			// internal node's thresholds and attributes should be the same
//			// leaf's labels should be the same
			if (!(dt2 instanceof DTNode)) {
				return false;
			}

			DTNode other = (DTNode) dt2;

			// Check leaf status
			if (this.leaf != other.leaf) {
				return false;
			}

			// Check label for leaf nodes
			if (this.leaf && this.label != (other.label)) {
				return false;
			}

			// Check attribute and threshold for internal nodes
			if (!this.leaf && (this.attribute != other.attribute || this.threshold != other.threshold)) {
				return false;
			}

			// Recursively check left and right children
			boolean leftEqual = (this.left == null) ? other.left == null : this.left.equals(other.left);
			boolean rightEqual = (this.right == null) ? other.right == null : this.right.equals(other.right);

			return leftEqual && rightEqual;
		}

	}



	//Given a dataset, this returns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist) {
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		return this.rootDTNode.classifyAtNode( xQuery );
	}

	// Checks the performance of a DecisionTree on a dataset
	// This method is provided in case you would like to compare your
	// results with the reference values provided in the PDF in the Data
	// section of the PDF
	String checkPerformance( ArrayList<Datum> datalist) {
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

//	private static void printNStr(int n, String str) {
//		for ( int i = 0 ; i< n ; i++ ) {
//			System.out.printf("%s",str);
//		}
//	}
//
//	private void logNode( DTNode currentNode, int depth, String leftOrRight) {
//        /*
//        boolean leaf;
//        int label = -1; // only defined if node is a leaf. red or green.
//        int attribute; // only defined if node is not a leaf, index i : x_i.
//        double threshold; // only defined if node is not a leaf : x_i < threshold. (Left) , x_i > threshold, right.
//        DTNode left, right; // the left and right child of a particular node. (null if leaf)
//        */
//
//		if ( currentNode.leaf ) {
//			printNStr( depth, " ");
//			System.out.printf("%s", "{ ");
//			System.out.printf("%s", "depth: " + depth + ",   "  );
//			System.out.printf("%s", "leaf: " + currentNode.leaf + ",   "   );
//			System.out.printf("%s", "label: " + currentNode.label + ",   "   );
//			System.out.printf("%s",leftOrRight + " },\n");
//			return;
//		}
//
//		printNStr( depth, " ");
//		System.out.printf("%s", "{ ");
//		System.out.printf("%s", "depth: " + depth + ",   "  );
//		System.out.printf("%s", "attribute: " + currentNode.attribute + ",   "  );
//		System.out.printf("%s", "threshold: " + currentNode.threshold + ",   "  );
//		System.out.printf("%s", "leaf: " + currentNode.leaf + ",   "   );
//		System.out.printf("%s",leftOrRight + " },\n");
//
//		logNode(currentNode.left, depth+1, "left");
//		logNode(currentNode.right, depth+1, "right");
//	}
//	void logTree() {
//		logNode(rootDTNode, 0, "root");
//	}

}

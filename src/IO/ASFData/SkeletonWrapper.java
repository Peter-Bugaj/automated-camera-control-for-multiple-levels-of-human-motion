package IO.ASFData;

import java.util.Hashtable;
import java.util.LinkedList;

public class SkeletonWrapper {

	/**Main root of the skeleton**/
	private SkeletonNode root = null;
	/**Nodes of the skeleton hashed by segment name**/
	private Hashtable<String, SkeletonNode> hashed_nodes = null;


	/**Class constructor**/
	public SkeletonWrapper(SkeletonNode root) {
		this.root=root;
		initHash();
	}
	/**Hash the skeleton nodes by segment name**/
	private void initHash() {
		LinkedList<SkeletonNode> root_stack = new LinkedList<SkeletonNode>();
		hashed_nodes = new Hashtable<String, SkeletonNode>();
		root_stack.push(root);
		
		while(root_stack.size() > 0) {
			SkeletonNode next_node = root_stack.pop();
			hashed_nodes.put(next_node.getName(), next_node);
			
			SkeletonNode[]children = next_node.getChildren();
			for(int i =0; i < children.length; i++) {
				root_stack.push(children[i]);
			}
		}
	}


	/**Get the skeleton root**/
	public SkeletonNode getRoot() {
		return root;
	}
	/**Get a node from within the skeleton using segment name**/
	public SkeletonNode getFromHash(String seg_name) {
		return hashed_nodes.get(seg_name);
	}
	/**Remove a node from the skeleton using segment name**/
	public void remopveFromHash(String seg_name) {
		hashed_nodes.remove(seg_name);
	}
}

package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.google.common.collect.ImmutableSet;

@SuppressWarnings("serial")
public class TreeFileChooserPanel extends JPanel implements FileChooserPanel {

	private final Set<String> typesToKeep = ImmutableSet.of("diseasephenotypes", "mirs", "transcriptionfactors", "drugtargets");
	
	private final JTree tree;
	private Consumer<Optional<String>> selectionListener = null;
	
	
	public TreeFileChooserPanel() {
		tree = createTree();
		
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.getSelectionModel().addTreeSelectionListener(this::fireSelection);
		setFiles(null);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}
	
	
	private JTree createTree() {
		return new JTree() {
			@Override
			public String convertValueToText(Object value, boolean selected, boolean expanded, 
					boolean leaf, int row, boolean hasFocus) {
				if(value instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
					Object userObject = treeNode.getUserObject();
					if(userObject instanceof GmtFile) {
						GmtFile gmtFile = (GmtFile) userObject;
						String fileName = gmtFile.getPath().getFileName().toString();
						String size = FileSizeRenderer.humanReadableByteCount(gmtFile.getSize(), true);
						return String.format("<html>%s <font color=#AAAAAA>(%s)</font></html>", fileName , size);
					}
				}
				return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
			}
		};
	}

	@Override
	public JPanel getPanel() {
		return this;
	}

	@Override
	public void setFiles(List<GmtFile> gmtFiles) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("download.baderlab.org");
		if(gmtFiles != null) {
			processFiles(0, root, gmtFiles);
		}
		TreeModel model = new DefaultTreeModel(root);
		tree.setModel(model);
	}
	
	
	private void processFiles(int depth, DefaultMutableTreeNode node, List<GmtFile> files) {
		Map<String,List<GmtFile>> paths = new TreeMap<>();
		for(GmtFile file : files) {
			Path path = file.getPath();
			if(depth < path.getNameCount()) {
				String segment = path.getName(depth).toString();
				if(depth != 2 || typesToKeep.contains(segment.toLowerCase())) {
					paths.computeIfAbsent(segment, r -> new ArrayList<>()).add(file);
				}
			} else {
				node.setUserObject(file);
			}
		}
		
		for(String segment : paths.keySet()) {
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(segment);
			node.add(child);
			processFiles(depth+1, child, paths.get(segment));
		}
	}
	

	@Override
	public Optional<String> getSelectedFilePath() {
		TreeSelectionModel selectionModel = tree.getSelectionModel();
		if(selectionModel.isSelectionEmpty())
			return Optional.empty();
		
		TreePath treePath = tree.getSelectionModel().getSelectionPath();
		if(!tree.getModel().isLeaf(treePath.getLastPathComponent()))
			return Optional.empty();
		
		GmtFile gmtFile = (GmtFile)((DefaultMutableTreeNode)treePath.getLastPathComponent()).getUserObject();
		return Optional.of(gmtFile.getPath().toString());
	}

	
	private void fireSelection(TreeSelectionEvent evt)  {
		if(selectionListener != null)
			selectionListener.accept(getSelectedFilePath());
	}
	
	@Override
	public void setSelectionListener(Consumer<Optional<String>> listener) {
		this.selectionListener = listener;
	}

}

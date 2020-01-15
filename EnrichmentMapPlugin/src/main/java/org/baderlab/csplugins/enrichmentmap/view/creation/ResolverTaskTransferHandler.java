package org.baderlab.csplugins.enrichmentmap.view.creation;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.TransferHandler;

@SuppressWarnings("serial")
public class ResolverTaskTransferHandler extends TransferHandler {

	private final Consumer<List<File>> consumer;
	
	public ResolverTaskTransferHandler(Consumer<List<File>> consumer) {
		this.consumer = Objects.requireNonNull(consumer);
	}
	
	@Override
	public boolean canImport(TransferSupport support) {
		if(!support.isDrop())
			return false;
		if(!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			return false;
		boolean copySupported = (COPY & support.getSourceDropActions()) == COPY;
		if(!copySupported)
			return false;
		return true;
	}
	
	@Override
	public boolean importData(TransferSupport support) {
		if(!canImport(support))
			return false;
		
		Object transferData;
		try {
			transferData = support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
			return false;
		}
		
		if(transferData instanceof List) {
			@SuppressWarnings("unchecked")
			List<File> fileList = (List<File>) transferData;
			consumer.accept(fileList);
			return true;
		}
		return false;
	}
	
}

package edu.nwmissouri.gdp.road;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import javax.swing.JPanel;

/*
 * @author Sahil Nokhwal
 * SID: S525352
 */
public class DropTargetPanel extends JPanel implements DropTargetListener {


		public DropTargetPanel(JPanel pane) {

		//	 this.pane = pane;
			 
			// dropTarget = new DropTarget(pane, DnDConstants.ACTION_COPY_OR_MOVE,
				//        this, true, null);

		}

		public void dragEnter(DropTargetDragEvent evt) {

			// Called when the user is dragging and enters this drop target

			System.out.println("Drop enter");

		}

		public void dragOver(DropTargetDragEvent evt) {

			// Called when the user is dragging and moves over this drop target

			System.out.println("Drop over");

		}

		public void dragExit(DropTargetEvent evt) {

			// Called when the user is dragging and leaves this drop target

			System.out.println("Drop exit");

		}

		public void dropActionChanged(DropTargetDragEvent evt) {

			// Called when the user changes the drag action between copy or move

			System.out.println("Drop action changed");

		}

		public void drop(DropTargetDropEvent evt) {

			// Called when the user finishes or cancels the drag operation

			try {

				Transferable transferable = evt.getTransferable();

				if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {

					evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					String dragContents = (String) transferable.getTransferData(DataFlavor.stringFlavor);

					evt.getDropTargetContext().dropComplete(true);

					// We append the label text to the text area when dropped

					//setText(getText() + " " + dragContents);

				} else {

					evt.rejectDrop();

				}

			} catch (IOException e) {

				evt.rejectDrop();

			} catch (UnsupportedFlavorException e) {

				evt.rejectDrop();

			}

		}

	}

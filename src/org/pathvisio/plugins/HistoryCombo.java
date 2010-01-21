// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.plugins;

import java.awt.Component;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.MutableComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class HistoryCombo extends JComboBox {

	public HistoryCombo() 
	{
		List<Object> history = new ArrayList<Object>();
		history.add ("idmapper-jdbc:mysql://localhost/snp?user=bridgedb");
		setModel(new FilterableComboBoxModel(history));
		setEditor(new MyEditor());
		setEditable(true);
	}

	public class FilterableComboBoxModel extends AbstractListModel
	implements MutableComboBoxModel {

		private List<Object> items;
		private Filter filter;
		private List<Object> filteredItems;
		private Object selectedItem;

		public FilterableComboBoxModel(List<Object> items) {

			this.items = new ArrayList<Object>(items);
			filteredItems = new ArrayList<Object>(items.size());
			updateFilteredItems();
		}

		public void addElement( Object obj ) {

			items.add(obj);
			updateFilteredItems();
		}

		public void removeElement( Object obj ) {

			items.remove(obj);
			updateFilteredItems();
		}

		public void removeElementAt(int index) {

			items.remove(index);
			updateFilteredItems();
		}

		public void insertElementAt( Object obj, int index ) {}

		public void setFilter(Filter filter) {

			this.filter = filter;
			updateFilteredItems();
		}

		protected void updateFilteredItems() {

			fireIntervalRemoved(this, 0, filteredItems.size());
			filteredItems.clear();

			if (filter == null)
				filteredItems.addAll(items);
			else {
				for (Object item : items) 
				{    
					if (filter.accept(item))
						filteredItems.add(item);
				}
			}
			fireIntervalAdded(this, 0, filteredItems.size());
		}

		public int getSize() {
			return filteredItems.size();
		}

		public Object getElementAt(int index) {
			return filteredItems.get(index);
		}

		public Object getSelectedItem() {
			return selectedItem;
		}

		public void setSelectedItem(Object val) {

			if ((selectedItem == null) && (val == null))
				return;

			if ((selectedItem != null) && selectedItem.equals(val))
				return;

			if ((val != null) && val.equals(selectedItem))
				return;

			selectedItem = val;
			fireContentsChanged(this, -1, -1);
		}
	}

	public static interface Filter {
		public boolean accept(Object obj);
	}

	class StartsWithFilter implements Filter {

		private String prefix;
		public StartsWithFilter(String prefix) { this.prefix = prefix; }
		public boolean accept(Object o) { return o.toString().startsWith(prefix); }
	}

	public class MyEditor implements ComboBoxEditor, DocumentListener {

		public JTextField text;
		private volatile boolean filtering = false;
		private volatile boolean setting = false;

		public MyEditor() {

			text = new JTextField(15);
			text.getDocument().addDocumentListener(this);
		}

		public Component getEditorComponent() { return text; }

		public void setItem(Object item) {

			if(filtering)
				return;

			setting = true;
			String newText = (item == null) ? "" : item.toString();
			text.setText(newText);
			setting = false;
		}

		public Object getItem() {
			return text.getText();
		}

		public void selectAll() { text.selectAll(); }

		public void addActionListener(ActionListener l) {
			text.addActionListener(l);
		}

		public void removeActionListener(ActionListener l) {
			text.removeActionListener(l);
		}

		public void insertUpdate(DocumentEvent e) { handleChange(); }
		public void removeUpdate(DocumentEvent e) { handleChange(); }
		public void changedUpdate(DocumentEvent e) { }

		protected void handleChange() {

			if (setting)
				return;

			filtering = true;

			Filter filter = null;
			if (text.getText().length() > 0)
				filter = new StartsWithFilter(text.getText());

			((FilterableComboBoxModel) getModel()).setFilter(filter);
			// A bit nasty but it seems to get the popup validated properly
			setPopupVisible(false);
			setPopupVisible(true);
			filtering = false;
		}
	}
}

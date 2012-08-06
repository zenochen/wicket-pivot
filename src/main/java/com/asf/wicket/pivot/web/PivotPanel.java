/*
 * Copyright 2012 Decebal Suiu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.asf.wicket.pivot.web;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;

import com.asf.wicket.pivot.DefaultPivotModel;
import com.asf.wicket.pivot.PivotDataSource;
import com.asf.wicket.pivot.PivotField;
import com.asf.wicket.pivot.PivotModel;

/**
 * @author Decebal Suiu
 */
public class PivotPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private WebMarkupContainer areasContainer;
	private PivotModel pivotModel;
	private PivotTable pivotTable;
	private AjaxLink<Void> computeLink;

	public PivotPanel(String id, PivotDataSource pivotDataSource) {
		super(id);
		
		// create a pivot model
		pivotModel = createPivotModel(pivotDataSource);
		
		// add some fields on some area
//		pivotModel.getField("REGION").setArea(PivotField.Area.ROW);
		/*
		pivotModel.getField("SALESMAN").setArea(PivotField.Area.ROW);
		pivotModel.getField("YEAR").setArea(PivotField.Area.COLUMN);
		pivotModel.getField("MONTH").setArea(PivotField.Area.COLUMN).setAreaIndex(1);
		pivotModel.getField("MONEY").setArea(PivotField.Area.DATA);
		*/

//		pivotModel.setShowGrandTotalForColumn(true);
//		pivotModel.setShowGrandTotalForRow(true);
		
		pivotModel.calculate();
		System.out.println(pivotModel);
		
		// debug
		/*
		Tree columnsHeaderTree =  pivotModel.getColumnsHeaderTree();
		System.out.println("### Columns Header Tree ###");
		TreeHelper.printTree(columnsHeaderTree.getRoot());
		TreeHelper.printLeafValues(columnsHeaderTree.getRoot());

		Tree rowsHeaderTree =  pivotModel.getRowsHeaderTree();
		System.out.println("### Rows Header Tree ### ");
		TreeHelper.printTree(rowsHeaderTree.getRoot());
		TreeHelper.printLeafValues(rowsHeaderTree.getRoot());
		*/
		
		areasContainer = new WebMarkupContainer("areas");
		areasContainer.setOutputMarkupId(true);
		add(areasContainer);
		
		RepeatingView areaRepeater = new RepeatingView("area");
		areasContainer.add(areaRepeater);
		List<PivotField.Area> areas = PivotField.Area.getValues();
		for (PivotField.Area area : areas) {
			areaRepeater.add(new PivotAreaPanel(areaRepeater.newChildId(), area));
		}
		
		pivotTable = new PivotTable("pivotTable", pivotModel);
//		pivotTable.setOutputMarkupId(true);
		pivotTable.setOutputMarkupPlaceholderTag(true);
		pivotTable.setVisible(false);
		add(pivotTable);
		
		AjaxCheckBox showGrandTotalForColumnCheckBox = new AjaxCheckBox("showGrandTotalForColumn", new PropertyModel<Boolean>(this, "pivotModel.showGrandTotalForColumn")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
			}
			
		};
		add(showGrandTotalForColumnCheckBox);

		AjaxCheckBox showGrandTotalForRowCheckBox = new AjaxCheckBox("showGrandTotalForRow", new PropertyModel<Boolean>(this, "pivotModel.showGrandTotalForRow")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
			}
			
		};
		add(showGrandTotalForRowCheckBox);

		computeLink = new IndicatingAjaxLink<Void>("compute") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (!verify()) {
					return;
				}
				
				pivotModel.calculate();
				PivotTable newPivotTable = new PivotTable("pivotTable", pivotModel);
				pivotTable.replaceWith(newPivotTable);
				pivotTable = newPivotTable;
				target.add(pivotTable);
			}

			/*
			@Override
			public boolean isEnabled() {
				return verify();
			}
			*/
			
		};
		computeLink.setOutputMarkupId(true);
		computeLink.add(AttributeModifier.append("class", new AbstractReadOnlyModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return verify() ? "btn-success" : "btn-success disabled";
			}
			
		}));
		add(computeLink);
	}

	protected PivotModel createPivotModel(PivotDataSource pivotDataSource) {
		return new DefaultPivotModel(pivotDataSource);
	}
	
	 @Override
	 public void onEvent(IEvent<?> event) {
         if (event.getPayload() instanceof AreaChangedEvent) {
        	 AjaxRequestTarget target = ((AreaChangedEvent) event.getPayload()).getAjaxRequestTarget();
        	 target.add(areasContainer);
        	 target.add(computeLink);
         }
	}

	public PivotModel getPivotModel() {
		return pivotModel;
	}

	private boolean verify() {
		return !pivotModel.getFields(PivotField.Area.COLUMN).isEmpty() &&
				!pivotModel.getFields(PivotField.Area.ROW).isEmpty()  &&
				!pivotModel.getFields(PivotField.Area.DATA).isEmpty();
	}

}
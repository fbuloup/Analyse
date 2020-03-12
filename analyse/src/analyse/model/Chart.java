/*******************************************************************************
 * Université d’Aix Marseille (AMU) - Centre National de la Recherche Scientifique (CNRS)
 * Copyright 2014 AMU-CNRS All Rights Reserved.
 * 
 * These computer program listings and specifications, herein, are
 * the property of Université d’Aix Marseille and CNRS
 * shall not be reproduced or copied or used in whole or in part as
 * the basis for manufacture or sale of items without written permission.
 * For a license agreement, please contact:
 * <mailto: licensing@sattse.com> 
 * 
 * Author : Frank BULOUP
 * Institut des Sciences du Mouvement - franck.buloup@univ-amu.fr
 ******************************************************************************/
package analyse.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.swt.graphics.Image;

import analyse.model.DataChart;
import analyse.Log;
import analyse.Utils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class Chart implements IResource, IResourceObserver {

	public static final String EXTENSION = ".chart";
	
	private String name;
	private String oldName;
	private IResource parent;
	private DataChart dataChart;

	public Chart(IResource selectedResource, String chartName, String chartType) {
		chartName = chartName.replaceAll(EXTENSION + "$", "");
		name = chartName + EXTENSION;
		oldName = name;
		parent = selectedResource;
		dataChart = new DataChart(chartType);
		saveChart();
	}
	
	public Chart(IResource selectedResource, String chartName) {
		chartName = chartName.replaceAll(EXTENSION + "$", "");
		name = chartName + EXTENSION;
		oldName = name;
		parent = selectedResource;
		File chartFile = new File(getAbsolutePath());
		if(chartFile.exists()) readChart(chartFile);
		else Log.logErrorMessage(Messages.getString("Chart.Error") + chartFile.getAbsolutePath());
	}
	
	protected void readChart(File chartFile) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(chartFile));
			dataChart = (DataChart) in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		} catch (ClassNotFoundException e) {
			Log.logErrorMessage(e);
		}
	}
	
	public void saveChart() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getAbsolutePath()));
			out.writeObject(dataChart);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
	}
	
	public void addResources(IResource[] resources) throws InterruptedException {
	}

	public void copyTo(IResource destResource) throws IOException, InterruptedException {
		File srcFile = new File(getAbsolutePath());
		File destFile = new File(destResource.getAbsolutePath() + File.separator + name);
		Utils.copyFile(srcFile, destFile);
		Chart chart = new Chart(destResource, name);
		destResource.addResources(new IResource[]{chart});
		chart.registerToExperimentsObservers();
	}

	public boolean delete() {
		boolean succeed = (new File(getAbsolutePath())).delete();
		if(succeed) getParent().remove(this);
		Experiments.getInstance().removeExperimentObserver(this);
		return succeed;
	}

	public String getAbsolutePath() {
		return parent.getAbsolutePath() + File.separator + name;
	}

	public IResource[] getChildren() {
		return null;
	}

	public Image getImage() {
		return ImagesUtils.getImage(IImagesKeys.CHART_EDITOR_ICON);
	}

	public String getName() {
		return name;
	}

	public String getOldName() {
		return oldName;
	}

	public IResource getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return false;
	}

	public boolean hasParent(IResource parentResource) {
		IResource parent = getParent();
		while(parent != null) {
			if(parent == parentResource) return true;
			parent = parent.getParent();
		}
		return false;
	}

	public void remove(IResource resource) {
	}

	public void rename(String newName) {
		String oldOldName = oldName;
		File oldFile = new File(getAbsolutePath());
		if(!oldName.equals(name)) oldName = name;
		name = newName + EXTENSION;
		File newFile = new File(getAbsolutePath());
		if(!oldFile.renameTo(newFile)) {
			name = oldName;
			oldName = oldOldName;
			Log.logErrorMessage(Messages.getString("Chart.ImpossibleRename") + getAbsolutePath());
		}
	}

	public String getLocalPath() {
		return parent.getLocalPath() + "." + getNameWithoutExtension();
	}
	
	public String getNameWithoutExtension() {
		return name.replaceAll(EXTENSION + "$", "");
	}
	
	public DataChart getData() {
		return dataChart;
	}

	public void update(int message, IResource[] resources) {
		if(message == IResourceObserver.RENAMED) {
			if(resources[0] instanceof Experiment || resources[0] instanceof Subject || resources[0] instanceof Signal) {
				if(dataChart.getChartType().equals(ChartsTypes.TIME_CHART_ID_STRING)) {
					String newlocalPath = resources[0].getLocalPath();
					String oldLocalPath = "";
					if(resources[0] instanceof Experiment) oldLocalPath = resources[0].getOldName();
					else oldLocalPath = resources[0].getParent().getLocalPath() + "." + resources[0].getOldName();
					String[] signals = dataChart.getSignals();
					for (int i = 0; i < signals.length; i++) {
						String oldSignal = signals[i];
						if(oldSignal.startsWith(oldLocalPath)) {
							String newSignal = oldSignal.replaceAll("^" + oldLocalPath, newlocalPath);
							dataChart.removeSignal(oldSignal);
							dataChart.addSignal(newSignal);
						}
					}
				}
				if(dataChart.getChartType().equals(ChartsTypes.XY_CHART_ID_STRING)) {
					String newlocalPath = resources[0].getLocalPath();
					String oldLocalPath = "";
					if(resources[0] instanceof Experiment) oldLocalPath = resources[0].getOldName();
					else oldLocalPath = resources[0].getParent().getLocalPath() + "." + resources[0].getOldName();
					String[] signals = dataChart.getSignals();
					for (int i = 0; i < signals.length; i++) {
						String oldSignal1 = signals[i].split(":")[0];
						String oldSignal2 = signals[i].split(":")[1];
						if(oldSignal1.startsWith(oldLocalPath) || oldSignal2.startsWith(oldLocalPath)) {
							String newSignal = oldSignal1.replaceAll("^" + oldLocalPath, newlocalPath) + ":" + oldSignal2.replaceAll("^" + oldLocalPath, newlocalPath);
							dataChart.removeSignal(signals[i]);
							dataChart.addSignal(newSignal);
						}
					}
				}
				saveChart();
			}
		}
		if(message == IResourceObserver.DELETED) {
			for (int i = 0; i < resources.length; i++) {
				if(resources[i] instanceof Subject) {
					String localPath = resources[i].getLocalPath();
					String[] signals = dataChart.getSignals();
					for (int j = 0; j < signals.length; j++) {
						String signal = signals[j];
						if(signal.startsWith(localPath)) dataChart.removeSignal(signal);
					}
					saveChart();
				}
			}
		}
	}

	public void registerToExperimentsObservers() {
		Experiments.getInstance().addExperimentObserver(this);
	}
	
	public IResource getFirstResourceByName(String resourceName) {
		if(getLocalPath().equals(resourceName)) return this;
		return null;
	}
	
	public Subject getSubjectByName(String resourceName) {
		return null;
	}

}

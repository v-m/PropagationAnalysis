package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.vmusco.smf.analysis.ProcessStatistics;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/*public class ObjectsPersistence extends ExecutionPersistence<ProcessStatistics>{

	private ProcessStatistics ps;

	public ObjectsPersistence(ProcessStatistics ps) {
		this.ps = ps;
	}

	@Override
	public ProcessStatistics loadState(String persistFile) throws IOException {
		File f = new File(persistFile);
		if(!f.exists()){
			return null;
		}
		
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);
		try {
			return (ProcessStatistics) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void saveState(ProcessStatistics ps) throws Exception {
		File f = new File(ps.persistFile);

		if(f.exists())
			f.delete();

		f.createNewFile();

		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(ps);

		oos.close();
		fos.close();	
	}

	@Override
	public void loadState(ProcessStatistics updateMe, String persistFile)
			throws Exception {
		throw new NotImplementedException();
	}

}*/

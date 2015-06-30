package com.vmusco.softminer.graphs.persistance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.vmusco.softminer.graphs.Graph;

public interface GraphPersistence {
	public void save(OutputStream os) throws IOException;
	public void load(InputStream is) throws IOException;
}

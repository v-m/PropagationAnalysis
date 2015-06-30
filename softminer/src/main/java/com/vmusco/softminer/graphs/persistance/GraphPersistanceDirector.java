package com.vmusco.softminer.graphs.persistance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.vmusco.softminer.graphs.Graph;

public interface GraphPersistanceDirector {
	void write(OutputStream out) throws IOException;
	void read(InputStream in) throws IOException;
}

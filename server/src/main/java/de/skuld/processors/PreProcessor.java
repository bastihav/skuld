package de.skuld.processors;

import de.skuld.web.model.Result;
import java.util.List;

public interface PreProcessor {
  List<byte[]> preprocess(Result result, List<byte[]> input);
}

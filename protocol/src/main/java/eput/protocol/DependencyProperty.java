package eput.protocol;

import java.util.List;

public interface DependencyProperty {
    void resolveDependencies(List<String> ids);
}

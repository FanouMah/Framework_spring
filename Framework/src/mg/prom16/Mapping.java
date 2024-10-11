package mg.prom16;

import java.util.HashSet;
import java.util.Set;
// import com.thoughtworks.paranamer.AdaptiveParanamer;
// import com.thoughtworks.paranamer.Paranamer;

public class Mapping {
    Set<Verb> verbs;

    public Mapping(){
        this.verbs = new HashSet<Verb>();
    }

    public void setVerbs(Set<Verb> verbs) {
        this.verbs = verbs;
    }

    public Set<Verb> getVerbs() {
        return verbs;
    }

    public void addVerb(Verb verb) throws Exception {
        for (Verb v : verbs) {
            if (v.getVerbAction().equalsIgnoreCase(verb.getVerbAction())) {
                throw new Exception("Conflit de verbe.");
            }
        }
        verbs.add(verb);
    }

    public Verb getByAction(String verbAction) {
        for (Verb v : verbs) {
            if (v.getVerbAction().equalsIgnoreCase(verbAction)) {
                return v;
            }
        }
        return null;
    }
}
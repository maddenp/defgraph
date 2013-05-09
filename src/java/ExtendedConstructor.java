import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

public class ExtendedConstructor extends Constructor
{
  public ExtendedConstructor()
  {
    this.yamlConstructors.put(new Tag("!unquoted"),new ConstructString());
  }

  private class ConstructString extends AbstractConstruct
  {
    public String construct(Node node)
    {
      return ((ScalarNode)node).getValue();
    }
  }
}

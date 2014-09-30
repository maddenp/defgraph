import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

public class ExtendedConstructor extends Constructor
{
  public ExtendedConstructor()
  {
    this.yamlConstructors.put(new Tag("!delete"  ),new ConstructTagged());
    this.yamlConstructors.put(new Tag("!replace" ),new ConstructTagged());
    this.yamlConstructors.put(new Tag("!unquoted"),new ConstructTagged());
  }

  private class ConstructTagged extends AbstractConstruct
  {
    public String construct(Node node)
    {
      if (node instanceof ScalarNode)
      {
        return ((ScalarNode)node).getValue();
      }
      return node.toString();
    }
  }
}

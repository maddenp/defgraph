import org.jgrapht.graph.DefaultEdge;

public class NullEdge extends DefaultEdge
{
  public String edgeLabel()
  {
    return super.toString();
  }
  public String toString()
  {
    return "";
  }
}

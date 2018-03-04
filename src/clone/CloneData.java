package clone;

public class CloneData {
public String path; 
public int startline;
public int endline;
public int LOC;
	public CloneData(String path, int startline, int endline, int LOC) {
		this.path = path;
		this.startline = startline;
		this.endline = endline;
		this.LOC = LOC;
	}

	@Override
	public String toString() {
		String toString = "";
		toString += "Path        : " + this.path;
		toString += "\nStartline   : " + this.startline;
		toString += "\nEndline     : " + this.endline;
		toString += "\nLength      : " + this.LOC;
		return toString;
	}
}

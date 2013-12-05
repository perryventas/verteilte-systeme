package go;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Christoph Friegel
 * @version 1.0
 */

@Path("tib")
public class Resources {
	// COUNT
	@Path("count")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response count() {
		Database ac = null;
		Count count = null;

		try {
			ac = new Database();
			count = ac.getClientCount(0);
		} catch (Exception e1) {
			System.out.println(e1);
			return null;
		}

		System.out.println("Count: " + count.getCountNr() + "; "
				+ count.getMaxDate() + "; " + count.getMinDate());
		return Response.ok(count).header("Access-Control-Allow-Origin", "*")
				.build();
	}

	/*
	 * @Path( "count" )
	 * 
	 * @GET
	 * 
	 * @Produces( MediaType.APPLICATION_JSON ) public Count count() { Database
	 * ac = null; Count count = null;
	 * 
	 * try { ac = new Database(); count = ac.getClientCount( 0 ); } catch
	 * (Exception e1) { System.out.println(e1); return null; }
	 * 
	 * System.out.println("Count: " + count.getCountNr() + "; " +
	 * count.getMaxDate() + "; " + count.getMinDate()); return count; }
	 */

	// TRACE
	@Path("trace")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response trace() {
		Database ac = null;

		try {
			ac = new Database();
			ac.deleteAllData();
		} catch (Exception e1) {
			System.out.println("Trace: " + e1);
			return Response.ok(0).header("Access-Control-Allow-Origin", "*")
					.build();
		}

		System.out.println("Trace: " + 1);
		return Response.ok(1).header("Access-Control-Allow-Origin", "*")
				.build();
	}

	/*
	 * @Path( "trace" )
	 * 
	 * @GET
	 * 
	 * @Produces( MediaType.TEXT_PLAIN ) public int trace() { Database ac =
	 * null;
	 * 
	 * try { ac = new Database(); ac.deleteAllData(); } catch (Exception e1) {
	 * System.out.println("Trace: " + e1); return 0; }
	 * 
	 * System.out.println("Trace: " + 1); return 1; }
	 */
}

//    	
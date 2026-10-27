package com.virtualpairprogrammers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.virtualpairprogrammers.tracker.data.Data;
import com.virtualpairprogrammers.tracker.domain.VehicleBuilder;
import com.virtualpairprogrammers.tracker.domain.VehicleNotFoundException;
import com.virtualpairprogrammers.tracker.domain.VehiclePosition;
import com.virtualpairprogrammers.tracker.rest.PositionReportsController;

public class TestPositionReportsController
{

	private Data data;
	private PositionReportsController controller;

	@Before
	public void setUp()
	{
		data = mock(Data.class);
		controller = new PositionReportsController();
		ReflectionTestUtils.setField(controller, "data", data);
	}

	@Test
	public void historyForUnknownVehicleReturnsNotFound() throws VehicleNotFoundException
	{
		when(data.getHistoryFor("unknown")).thenThrow(new VehicleNotFoundException());

		ResponseEntity<Collection> response = controller.getEntireHistoryForVehicle("unknown");

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
	}

	@Test
	public void historyForKnownVehicleReturnsOkWithOriginalPositions() throws VehicleNotFoundException
	{
		VehiclePosition first = new VehicleBuilder().withName("truck")
				.withLat("1.0").withLng("2.0").withTimestamp(new Date(1000L)).build();
		VehiclePosition second = new VehicleBuilder().withName("truck")
				.withLat("3.0").withLng("4.0").withTimestamp(new Date(2000L)).build();
		Collection<VehiclePosition> positions = Arrays.asList(first, second);
		when(data.getHistoryFor("truck")).thenReturn(positions);

		ResponseEntity<Collection> response = controller.getEntireHistoryForVehicle("truck");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(positions, response.getBody());
	}

	@Test
	public void latestPositionForUnknownVehicleReturnsNotFound() throws VehicleNotFoundException
	{
		when(data.getLatestPositionFor("unknown")).thenThrow(new VehicleNotFoundException());

		ResponseEntity<VehiclePosition> response = controller.getLatestReportForVehicle("unknown");

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
	}
}

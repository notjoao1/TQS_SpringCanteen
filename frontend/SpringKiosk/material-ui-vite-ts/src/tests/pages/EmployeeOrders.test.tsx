// src/tests/EmployeeOrders.test.tsx
import { render, screen } from '@testing-library/react';
import { describe, test, expect, beforeEach, afterEach } from 'vitest';
import { Server } from 'mock-socket';
import { debug } from "vitest-preview";

import mockOrders from "../mock/mockOrders";
import EmployeeOrders from '../../pages/EmployeeOrders';
import config from "../../config";

describe('EmployeeOrders websocket UT', () => {
  let mockServer: Server;

  beforeEach(() => {
    mockServer = new Server(config.ordersWebSocketUrl);

    // Send a mock message when the connection is established
    mockServer.on('connection', (socket) => {
      console.log("Connected to WebSocket");
      setTimeout(() => {
        mockOrders.forEach((order) =>
          socket.send(JSON.stringify(order))
        )
      }, 1000);
    });
  });

  afterEach(() => {
    mockServer.stop();
  });

  test('renders mock orders from WebSocket', async () => {
    render(<EmployeeOrders />);

    const menuText = await screen.findByText('Breakfast Menu');

    debug();

    expect(menuText).toBeTruthy();
  });
});

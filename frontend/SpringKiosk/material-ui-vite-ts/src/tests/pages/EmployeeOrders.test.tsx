import React from 'react';
import { render, screen } from '@testing-library/react';
import { afterAll, beforeAll, describe, expect, it, vi } from 'vitest';
import { Server } from 'mock-socket';
import EmployeeOrders from '../../pages/EmployeeOrders';
import { OrderStatus } from '../../types/OrderTypes';
import config from "../../config";
import mockOrders from '../mock/mockOrders';
import { debug } from 'vitest-preview';

describe('EmployeeOrders WebSocket Tests', () => {
  let ordersMockServer: Server;

  beforeAll(() => {
    ordersMockServer = new Server(config.ordersWebSocketUrl);

    ordersMockServer.on('connection', socket => {
      setTimeout( () =>
      mockOrders.forEach(
        order => socket.send(JSON.stringify(order))
      ), 100);
    });
  });

  afterAll(() => {
    ordersMockServer.stop();
  });

  it('renders mock orders from WebSocket', () => {
    render(<EmployeeOrders />);

    expect(screen.findByText("101")).toBeTruthy();
    expect(screen.findByText("Breakfast Menu")).toBeTruthy();
    
    debug();
  });
});

import { useEffect, useMemo, useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Select from "@mui/material/Select";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import VisibilityIcon from "@mui/icons-material/Visibility";
import IconButton from "@mui/material/IconButton";
import CircularProgress from "@mui/material/CircularProgress";
import useAuth from "../hooks/useAuth";
import orderService from "../services/orderService";

const ORDER_STATUSES = ["PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"];

const OrdersPage = () => {
  const { user, loadingProfile } = useAuth();
  const isAdmin = useMemo(() => user?.roles?.includes("ROLE_ADMIN"), [user]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [detailOrder, setDetailOrder] = useState(null);
  const [updating, setUpdating] = useState(false);

  const fetchOrders = async () => {
    if (!user) {
      return;
    }

    setLoading(true);
    setError(null);
    try {
      let data;
      if (isAdmin) {
        try {
          data = await orderService.getAll();
        } catch (adminError) {
          if (adminError.response?.status === 404 && user.customerId) {
            data = await orderService.getForCustomer(user.customerId);
          } else {
            throw adminError;
          }
        }
      } else {
        data = await orderService.getForCustomer(user.customerId);
      }
      setOrders(data ?? []);
    } catch (err) {
      setError(err?.response?.data?.message ?? "Unable to load orders");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if ((isAdmin || user?.customerId) && !loadingProfile) {
      fetchOrders().catch(() => undefined);
    }
  }, [isAdmin, user?.customerId, loadingProfile]);

  const handleStatusChange = async (orderId, status) => {
    setUpdating(true);
    setError(null);
    try {
      await orderService.updateStatus(orderId, status);
      await fetchOrders();
    } catch (err) {
      setError(err?.response?.data?.message ?? "Unable to update order status");
    } finally {
      setUpdating(false);
    }
  };

  if (!user) {
    return <Alert severity="info">Login to view your orders.</Alert>;
  }

  if (!isAdmin && !user.customerId) {
    return <Alert severity="warning">We could not find your customer profile. Contact support.</Alert>;
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h4">{isAdmin ? "Orders dashboard" : "Your orders"}</Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      {loading ? (
        <Box display="flex" justifyContent="center" py={4}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Customer</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Total</TableCell>
                <TableCell align="center">Created</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {orders.map((order) => (
                <TableRow key={order.id} hover>
                  <TableCell>{order.id}</TableCell>
                  <TableCell>{order.customerId}</TableCell>
                  <TableCell>
                    {isAdmin ? (
                      <FormControl size="small" sx={{ minWidth: 140 }}>
                        <InputLabel id={`status-${order.id}`}>Status</InputLabel>
                        <Select
                          labelId={`status-${order.id}`}
                          label="Status"
                          value={order.status}
                          onChange={(event) => handleStatusChange(order.id, event.target.value)}
                          disabled={updating}
                        >
                          {ORDER_STATUSES.map((status) => (
                            <MenuItem key={status} value={status}>
                              {status}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    ) : (
                      <Typography>{order.status}</Typography>
                    )}
                  </TableCell>
                  <TableCell align="right">
                    ${Number(order.totalAmount ?? 0).toFixed(2)}
                  </TableCell>
                  <TableCell align="center">
                    {order.createdAt ? new Date(order.createdAt).toLocaleString() : "-"}
                  </TableCell>
                  <TableCell align="right">
                    <IconButton color="primary" onClick={() => setDetailOrder(order)}>
                      <VisibilityIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {!orders.length ? (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    No orders available.
                  </TableCell>
                </TableRow>
              ) : null}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={Boolean(detailOrder)} onClose={() => setDetailOrder(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Order #{detailOrder?.id}</DialogTitle>
        <DialogContent>
          <Stack spacing={2}>
            <Typography variant="body2">Customer: {detailOrder?.customerId}</Typography>
            <Typography variant="body2">Status: {detailOrder?.status}</Typography>
            <Typography variant="body2">Created: {detailOrder?.createdAt ? new Date(detailOrder.createdAt).toLocaleString() : "-"}</Typography>
            <Typography variant="body2">Total: ${Number(detailOrder?.totalAmount ?? 0).toFixed(2)}</Typography>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Product</TableCell>
                    <TableCell align="right">Quantity</TableCell>
                    <TableCell align="right">Price</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {detailOrder?.items?.map((item) => (
                    <TableRow key={item.productId}>
                      <TableCell>{item.productName}</TableCell>
                      <TableCell align="right">{item.quantity}</TableCell>
                      <TableCell align="right">${Number(item.priceAtPurchase ?? 0).toFixed(2)}</TableCell>
                    </TableRow>
                  ))}
                  {!detailOrder?.items?.length ? (
                    <TableRow>
                      <TableCell colSpan={3} align="center">
                        No items in this order.
                      </TableCell>
                    </TableRow>
                  ) : null}
                </TableBody>
              </Table>
            </TableContainer>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailOrder(null)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
};

export default OrdersPage;

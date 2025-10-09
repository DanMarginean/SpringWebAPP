import { useEffect, useMemo, useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Grid from "@mui/material/Grid";
import IconButton from "@mui/material/IconButton";
import LinearProgress from "@mui/material/LinearProgress";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import AddShoppingCartIcon from "@mui/icons-material/AddShoppingCart";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import useAuth from "../hooks/useAuth";
import cartService from "../services/cartService";

const CartPage = () => {
  const { user, loadingProfile } = useAuth();
  const customerId = user?.customerId;
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({ productId: "", quantity: 1 });
  const [quantityDialog, setQuantityDialog] = useState({ open: false, productId: null, quantity: 1 });

  const canManageCart = useMemo(() => user?.roles?.includes("ROLE_CUSTOMER"), [user]);

  const loadCart = async () => {
    if (!customerId) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const data = await cartService.getCart(customerId);
      setCart(data);
    } catch (err) {
      setError(err?.response?.data?.message ?? "Unable to load cart");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (customerId) {
      loadCart().catch(() => undefined);
    }
  }, [customerId]);

  const handleFormChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleAddItem = async (event) => {
    event.preventDefault();
    if (!customerId) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await cartService.addItem(customerId, {
        productId: Number(form.productId),
        quantity: Number(form.quantity)
      });
      setForm({ productId: "", quantity: 1 });
      await loadCart();
    } catch (err) {
      setError(err?.response?.data?.message ?? "Unable to add item");
      setLoading(false);
    }
  };

  const openQuantityDialog = (item) => {
    setQuantityDialog({ open: true, productId: item.productId, quantity: item.quantity });
  };

  const handleQuantitySave = async () => {
    if (!customerId || !quantityDialog.productId) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await cartService.updateItem(customerId, quantityDialog.productId, quantityDialog.quantity);
      setQuantityDialog({ open: false, productId: null, quantity: 1 });
      await loadCart();
    } catch (err) {
      setError(err?.response?.data?.message ?? "Unable to update item");
      setLoading(false);
    }
  };

  const handleRemove = async (productId) => {
    if (!customerId) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await cartService.removeItem(customerId, productId);
      await loadCart();
    } catch (err) {
      setError(err?.response?.data?.message ?? "Unable to remove item");
      setLoading(false);
    }
  };

  if (!canManageCart) {
    return (
      <Alert severity="info">Login as a customer to manage the cart.</Alert>
    );
  }

  if (loadingProfile || (!customerId && user)) {
    return <Typography>Loading your profile...</Typography>;
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h4">Your cart</Typography>
      {loading ? <LinearProgress /> : null}
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Box component="form" onSubmit={handleAddItem}>
        <Paper sx={{ p: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={5}>
              <TextField
                label="Product ID"
                name="productId"
                value={form.productId}
                onChange={handleFormChange}
                required
                fullWidth
              />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField
                label="Quantity"
                name="quantity"
                type="number"
                value={form.quantity}
                onChange={handleFormChange}
                inputProps={{ min: 1 }}
                required
                fullWidth
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <Button
                type="submit"
                variant="contained"
                startIcon={<AddShoppingCartIcon />}
                disabled={loading}
                fullWidth
              >
                Add to cart
              </Button>
            </Grid>
          </Grid>
        </Paper>
      </Box>

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Product</TableCell>
              <TableCell align="right">Quantity</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cart?.items?.map((item) => (
              <TableRow key={item.productId} hover>
                <TableCell>
                  <strong>{item.productName}</strong>
                </TableCell>
                <TableCell align="right">{item.quantity}</TableCell>
                <TableCell align="right">
                  <IconButton color="primary" onClick={() => openQuantityDialog(item)}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                  <IconButton color="error" onClick={() => handleRemove(item.productId)}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!cart?.items?.length ? (
              <TableRow>
                <TableCell colSpan={3} align="center">
                  Your cart is empty.
                </TableCell>
              </TableRow>
            ) : null}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={quantityDialog.open} onClose={() => setQuantityDialog({ open: false, productId: null, quantity: 1 })}>
        <DialogTitle>Update quantity</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Quantity"
            type="number"
            value={quantityDialog.quantity}
            onChange={(event) =>
              setQuantityDialog((current) => ({ ...current, quantity: Number(event.target.value) || 1 }))
            }
            inputProps={{ min: 0 }}
            fullWidth
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setQuantityDialog({ open: false, productId: null, quantity: 1 })}>Cancel</Button>
          <Button variant="contained" onClick={handleQuantitySave}>
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
};

export default CartPage;

import { useEffect, useMemo, useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Grid from "@mui/material/Grid";
import IconButton from "@mui/material/IconButton";
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
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import useAuth from "../hooks/useAuth";
import productService from "../services/productService";

const emptyProduct = {
  name: "",
  price: "",
  description: "",
  stockQuantity: "",
  category: ""
};

const ProductsPage = () => {
  const { user } = useAuth();
  const isAdmin = useMemo(() => user?.roles?.includes("ROLE_ADMIN"), [user]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState(emptyProduct);
  const [editingProductId, setEditingProductId] = useState(null);
  const [confirmProduct, setConfirmProduct] = useState(null);
  const [saving, setSaving] = useState(false);

  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await productService.getAll();
      setProducts(data ?? []);
    } catch (err) {
      setError(err?.response?.data?.message ?? "Unable to load products");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts().catch(() => undefined);
  }, []);

  const handleDialogClose = () => {
    setDialogOpen(false);
    setForm(emptyProduct);
    setEditingProductId(null);
  };

  const handleFormChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const openForCreate = () => {
    setForm(emptyProduct);
    setEditingProductId(null);
    setDialogOpen(true);
  };

  const openForEdit = (product) => {
    setForm({
      name: product.name ?? "",
      price: product.price ?? "",
      description: product.description ?? "",
      stockQuantity: product.stockQuantity ?? "",
      category: product.category ?? ""
    });
    setEditingProductId(product.id);
    setDialogOpen(true);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity)
      };
      if (editingProductId) {
        await productService.update(editingProductId, payload);
      } else {
        await productService.create(payload);
      }
      handleDialogClose();
      await loadProducts();
    } catch (err) {
      setError(err?.response?.data?.message ?? "Unable to save product");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!confirmProduct) {
      return;
    }
    try {
      await productService.remove(confirmProduct.id);
      setConfirmProduct(null);
      await loadProducts();
    } catch (err) {
      setError(err?.response?.data?.message ?? "Unable to delete product");
    }
  };

  return (
    <Stack spacing={3}>
      <Box display="flex" alignItems="center" justifyContent="space-between">
        <Typography variant="h4">Products</Typography>
        {isAdmin ? (
          <Button variant="contained" startIcon={<AddCircleIcon />} onClick={openForCreate}>
            Add product
          </Button>
        ) : null}
      </Box>
      {error ? <Alert severity="error">{error}</Alert> : null}
      {loading ? (
        <Box display="flex" justifyContent="center" py={5}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Category</TableCell>
                <TableCell align="right">Price</TableCell>
                {isAdmin ? <TableCell align="right">Actions</TableCell> : null}
              </TableRow>
            </TableHead>
            <TableBody>
              {products.map((product) => (
                <TableRow key={product.id} hover>
                  <TableCell>{product.name}</TableCell>
                  <TableCell>{product.category}</TableCell>
                  <TableCell align="right">${Number(product.price).toFixed(2)}</TableCell>
                  {isAdmin ? (
                    <TableCell align="right">
                      <IconButton color="primary" onClick={() => openForEdit(product)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                      <IconButton color="error" onClick={() => setConfirmProduct(product)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  ) : null}
                </TableRow>
              ))}
              {!products.length ? (
                <TableRow>
                  <TableCell colSpan={isAdmin ? 4 : 3} align="center">
                    No products found.
                  </TableCell>
                </TableRow>
              ) : null}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogOpen} onClose={handleDialogClose} fullWidth maxWidth="sm" component="form" onSubmit={handleSubmit}>
        <DialogTitle>{editingProductId ? "Update product" : "Add product"}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField
                label="Name"
                name="name"
                value={form.name}
                onChange={handleFormChange}
                required
                fullWidth
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                label="Price"
                name="price"
                value={form.price}
                onChange={handleFormChange}
                type="number"
                inputProps={{ step: "0.01" }}
                required
                fullWidth
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                label="Stock"
                name="stockQuantity"
                value={form.stockQuantity}
                onChange={handleFormChange}
                type="number"
                required
                fullWidth
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                label="Category"
                name="category"
                value={form.category}
                onChange={handleFormChange}
                required
                fullWidth
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Description"
                name="description"
                value={form.description}
                onChange={handleFormChange}
                multiline
                minRows={3}
                fullWidth
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDialogClose}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={saving}>
            {saving ? "Saving..." : "Save"}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(confirmProduct)} onClose={() => setConfirmProduct(null)}>
        <DialogTitle>Delete product</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete <strong>{confirmProduct?.name}</strong>?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmProduct(null)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={handleDelete}>
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
};

export default ProductsPage;

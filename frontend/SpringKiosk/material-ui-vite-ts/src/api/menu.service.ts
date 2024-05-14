import axios from "axios"

export const fetchAllMenus = async () => {
    const res = await axios.get("http://localhost:8080/api/menus");
    console.log("all menus:", res.data)
}
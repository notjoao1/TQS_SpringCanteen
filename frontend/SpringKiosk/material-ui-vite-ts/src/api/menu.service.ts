import axios from "axios"
import { IMenu } from "../types/MenuTypes";

export const fetchAllMenus = async (): Promise<IMenu[]> => {
    const res = await axios.get<IMenu[]>("http://localhost:8080/api/menus");

    return res.data;
}
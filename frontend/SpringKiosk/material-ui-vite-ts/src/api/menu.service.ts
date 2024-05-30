import axios from "axios"
import { IMenu } from "../types/MenuTypes";

export const fetchAllMenus = async (): Promise<IMenu[]> => {
    const VITE_HOST = import.meta.env.VITE_HOST as string;
    const res = await axios.get<IMenu[]>(`http://${VITE_HOST}/api/menus`);

    return res.data;
}